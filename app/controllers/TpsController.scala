/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import actors.{UtrCacheActor, UtrCacheCommandActor}
import actors.UtrCacheCommandActor.VerifyUtr
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.{ByteString, Timeout}

import java.time.LocalDateTime
import auth.Actions
import connectors.EmailConnector
import model.Utr.{AllGood, DecryptedUtrFile, Denied, MissingFile, Utr, VerifyUtrRequest, VerifyUtrStatus}

import javax.inject.{Inject, Singleton}
import model._
import model.pcipal.ChargeRefNotificationPcipalRequest
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repository.{TpsRepo}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import services.UtrFileService
import scala.language.postfixOps

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TpsController @Inject() (actions:        Actions,
                               cc:             ControllerComponents,
                               tpsRepo:        TpsRepo,
                               emailConnector: EmailConnector,
                               utrFileService: UtrFileService
)(implicit executionContext: ExecutionContext, system: ActorSystem) extends BackendController(cc) {

  val logger: Logger = Logger(this.getClass)

  implicit val timeout: Timeout = Timeout(20 seconds)

  //TODO Inject actor
  val utrCacheActor = system.actorOf(UtrCacheActor.props())
  val utrCacheCommandActor = system.actorOf(UtrCacheCommandActor.props(utrCacheActor, utrFileService))

  //  onStart()

  val createTpsPayments: Action[TpsPaymentRequest] = actions.strideAuthenticateAction().async(parse.json[TpsPaymentRequest]) { implicit request =>
    val tpsPayments = request.body.tpsPayments(LocalDateTime.now())

    tpsRepo.upsert(tpsPayments._id, tpsPayments).map { _ =>
      Created(toJson(tpsPayments._id))
    }
  }

  def storeTpsPayments(): Action[TpsPayments] = actions.strideAuthenticateAction().async(parse.json[TpsPayments]) { implicit request =>
    val updatedPayments = request.body.payments map (payment => payment.copy(paymentItemId = Some(PaymentItemId.fresh)))

    tpsRepo.upsert(request.body._id, request.body.copy(payments = updatedPayments)).map { _ =>
      Ok(toJson(request.body._id))
    }
  }

  def findTpsPayments(id: TpsId): Action[AnyContent] = actions.strideAuthenticateAction().async {
    logger.debug(s"findTpsPayments received vrn $id")

    tpsRepo.findPayment(id).map {
      case Some(x) => Ok(toJson(x))
      case None    => NotFound(s"No payments found for id ${id.value}")
    }
  }

  def getId: Action[AnyContent] = actions.strideAuthenticateAction().async {
    logger.debug(s"getId")
    Future.successful(Ok(toJson(TpsId.fresh)))
  }

  def getTaxType(id: PaymentItemId): Action[AnyContent] = Action.async {
    logger.debug(s"getPaymentItem ${id.value}")

    tpsRepo.findPaymentItem(id).map {
      case Some(paymentItem) => Ok(toJson(paymentItem.taxType))
      case None              => NotFound(s"No payment item found for id ${id.value}")
    }
  }

  def delete(tpsId: TpsId): Action[AnyContent] = actions.strideAuthenticateAction().async {
    logger.debug(s"delete, id= ${tpsId.value}")
    tpsRepo.removeById(tpsId).map(_ => Ok)
  }

  def updateWithPcipalSessionId(): Action[UpdateRequest] = actions.strideAuthenticateAction().async(parse.json[UpdateRequest]) { implicit request =>
    logger.debug(s"updateWithPcipalSessionId, update= ${request.body.toString}")
    for {
      record <- tpsRepo.getPayment(request.body.tpsId)
      newRecord = record.copy(pciPalSessionId = Some(request.body.pcipalSessionId))
      _ <- tpsRepo.upsert(request.body.tpsId, newRecord)
    } yield {
      Ok
    }
  }

  def updateWithPcipalData(): Action[ChargeRefNotificationPcipalRequest] = Action.async(parse.json[ChargeRefNotificationPcipalRequest]) { implicit request =>
    logger.debug(s"updateWithPcipalSessionId, update= ${request.body.toString}")

    val f = for {
      a <- tpsRepo.findByPcipalSessionId(request.body.PCIPalSessionId)
      _ = sendEmails(a)
      _ <- tpsRepo.upsert(a._id, updateTpsPayments(a, request.body))
    } yield Ok

    f.recover {
      case e: IdNotFoundException => BadRequest(e.getMessage)
    }
  }

  def verifyUtr(): Action[VerifyUtrRequest] = Action.async(parse.json[VerifyUtrRequest]) { implicit request =>
    val utr: Utr = request.body.utr
    logger.info("verifyUtr UTR request received")
    (utrCacheCommandActor ? VerifyUtr(utr)).mapTo[VerifyUtrStatus].map {
      case AllGood     => Ok
      case Denied      => Forbidden
      case MissingFile => InternalServerError("UTR file is missing, please upload")
    }
  }

  def uploadDeniedUtrs(): Action[ByteString] = Action.async(parse.byteString) { implicit request =>
    val decryptedFile = DecryptedUtrFile(request.body.utf8String)

    logger.info("upload UTR file request received")

    for {
      _ <- utrFileService.parseAndValidateDecryptedUtrFile(decryptedFile)
      _ <- utrFileService.insertUtrFile(decryptedFile)
      _ <- utrFileService.removeObsoleteFiles //TODO check if we want that or want to delete
    } yield Ok

  }

  private def updateTpsPayments(tpsPayments: TpsPayments, chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest): TpsPayments = {
    logger.debug("updateTpsPayments")
    val remainder = tpsPayments.payments.filterNot(f => f.paymentItemId.contains(chargeRefNotificationPciPalRequest.paymentItemId))
    val update = tpsPayments.payments.filter(f => f.paymentItemId.contains(chargeRefNotificationPciPalRequest.paymentItemId))

    val tpsPaymentsListNew = update.headOption match {
      case Some(singleUpdate) =>
        val updated = singleUpdate.copy(pcipalData = Some(chargeRefNotificationPciPalRequest))
        remainder.::(updated)
      case None => throw new IdNotFoundException(s"Could not find paymentItemId: ${chargeRefNotificationPciPalRequest.paymentItemId.value}")
    }

    tpsPayments.copy(payments = tpsPaymentsListNew)
  }

  private def sendEmails(tpsPayments: TpsPayments)(implicit hc: HeaderCarrier): Unit = {
    tpsPayments.payments.map{ nextPaymentItem =>
      emailConnector.sendEmail(
        languageCode     = "en",
        email            = nextPaymentItem.email,
        displayTaxType   = nextPaymentItem.taxType.toString,
        paymentReference = nextPaymentItem.paymentSpecificData.getReference,
        amountPaid       = nextPaymentItem.amount)
    }
    ()
  }
}
