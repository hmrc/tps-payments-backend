/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.LocalDateTime

import auth.Actions
import connectors.EmailConnector
import javax.inject.{Inject, Singleton}
import model._
import model.pcipal.ChargeRefNotificationPcipalRequest
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repository.TpsRepo
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TpsController @Inject() (actions: Actions,
                               cc:      ControllerComponents,
                               tpsRepo: TpsRepo,
                               emailConnector: EmailConnector)(implicit executionContext: ExecutionContext) extends BackendController(cc) {

  val logger: Logger = Logger(this.getClass)

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
      _ = maybeSendEmails(a)
      _ <- tpsRepo.upsert(a._id, updateTpsPayments(a, request.body))
    } yield Ok

    f.recover {
      case e: IdNotFoundException => BadRequest(e.getMessage)
    }
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
  
  private def maybeSendEmails(tpsPayments: TpsPayments)(implicit hc: HeaderCarrier): Unit = {
     logger.debug("maybeSendEmails")
     tpsPayments.payments.find(p => p.email.nonEmpty)
       .fold(())(tpsPaymentItem => sendEmail(tpsPaymentItem))
     ()
   }
   //Purely exists to avoid 'discard non-unit value' compiler error
   private def sendEmail(tpsPaymentItem: TpsPaymentItem)(implicit hc: HeaderCarrier): Unit = {
     emailConnector.sendEmail(
       languageCode     = tpsPaymentItem.languageCode.getOrElse(throw new RuntimeException("maybeSendEmails error: no language code was present")),
       email            = tpsPaymentItem.email.getOrElse(throw new RuntimeException("maybeSendEmails error: email should be present but isn't")),
       displayTaxType   = tpsPaymentItem.taxType.toString,
       paymentReference = tpsPaymentItem.paymentSpecificData.getReference,
       amountPaid       = tpsPaymentItem.amount)
     ()
   }
}
