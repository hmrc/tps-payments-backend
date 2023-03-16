/*
 * Copyright 2023 HM Revenue & Customs
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

import auth.Actions
import model._
import model.pcipal.ChargeRefNotificationPcipalRequest
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repository.TpsPaymentsRepo
import services.EmailService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import util.EmailCrypto

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TpsController @Inject() (actions:      Actions,
                               cc:           ControllerComponents,
                               tpsRepo:      TpsPaymentsRepo,
                               emailService: EmailService,
                               emailCrypto:  EmailCrypto)(implicit executionContext: ExecutionContext) extends BackendController(cc) {

  val logger: Logger = Logger(this.getClass)

  val createTpsPayments: Action[TpsPaymentRequest] = actions.strideAuthenticateAction().async(parse.json[TpsPaymentRequest]) { implicit request =>
    val tpsPayments = request.body.tpsPayments(Instant.now())

    tpsRepo.upsert(tpsPayments).map { _ =>
      Created(toJson(tpsPayments._id))
    }
  }

  def storeTpsPayments(): Action[TpsPayments] = actions.strideAuthenticateAction().async(parse.json[TpsPayments]) { implicit request =>
    val updatedPayments = request.body.payments map (payment => payment.copy(paymentItemId = Some(PaymentItemId.fresh)))
    val updatedPaymentsWithEncryptedEmails = updatedPayments.map(tpsPaymentItem => tpsPaymentItem.email match {
      case Some(email) => tpsPaymentItem.copy(email = Some(emailCrypto.encryptEmailIfNotAlreadyEncrypted(email)))
      case _           => tpsPaymentItem
    })

    tpsRepo.upsert(request.body.copy(payments = updatedPaymentsWithEncryptedEmails)).map { _ =>
      Ok(toJson(request.body._id))
    }
  }

  def findTpsPayments(id: TpsId): Action[AnyContent] = actions.strideAuthenticateAction().async {
    logger.debug(s"findTpsPayments received vrn ${id.value}")
    tpsRepo.findPayment(id).map {
      case Some(tpsPayments) => Ok(toJson(tpsPayments))
      case None              => NotFound(s"No payments found for id ${id.value}")
    }
  }

  def findTpsPaymentsWithDecryptedEmail(id: TpsId): Action[AnyContent] = actions.strideAuthenticateAction().async {
    logger.debug(s"findTpsPaymentsWithDecryptedEmail received vrn ${id.value}")

    tpsRepo.findPayment(id).map {
      case Some(tpsPayments) =>
        val tpsPaymentItemsWithDecryptedEmail = tpsPayments.payments.map(
          nextTpsPaymentItem => nextTpsPaymentItem.copy(email = emailCrypto.maybeDecryptEmail(nextTpsPaymentItem.email)))
        Ok(toJson(tpsPayments.copy(payments = tpsPaymentItemsWithDecryptedEmail)))
      case None => NotFound(s"No payments found for id ${id.value}")
    }
  }

  def getId: Action[AnyContent] = actions.strideAuthenticateAction().async {
    logger.debug(s"getId")
    Future.successful(Ok(toJson(TpsId.fresh)))
  }

  def getTaxType(id: PaymentItemId): Action[AnyContent] = Action.async {
    logger.debug(s"getPaymentItem ${id.value}")

    tpsRepo.findPaymentItem(id).map {
      case Some(paymentItem) =>
        logger.debug("taxType found:" + paymentItem.taxType.toString)
        Ok(toJson(paymentItem.taxType))
      case None =>
        logger.debug("taxType not found")
        NotFound(s"No payment item found for id ${id.value}")
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
      _ <- tpsRepo.upsert(newRecord)
    } yield {
      Ok
    }
  }

  def updateWithPcipalData(): Action[ChargeRefNotificationPcipalRequest] = Action.async(parse.json[ChargeRefNotificationPcipalRequest]) { implicit request =>
    logger.debug(s"updateWithPcipalData, update= ${request.body.toString}")

    val f = for {
      existingTpsPayments <- tpsRepo.findByPcipalSessionId(request.body.PCIPalSessionId)
      updatedTpsPayments = updateTpsPayments(existingTpsPayments, request.body)
      _ <- tpsRepo.upsert(updatedTpsPayments)
      _ = emailService.maybeSendEmail(updatedTpsPayments.payments)
    } yield Ok

    f.recover {
      case e: IdNotFoundException => BadRequest(e.getMessage)
    }
  }

  private def updateTpsPayments(tpsPayments: TpsPayments, chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest): TpsPayments = {
    logger.debug("updateTpsPayments")
    val remainder: List[TpsPaymentItem] = tpsPayments.payments.filterNot(nextTpsPaymentItem => nextTpsPaymentItem.paymentItemId.contains(chargeRefNotificationPciPalRequest.paymentItemId))
    val update: List[TpsPaymentItem] = tpsPayments.payments.filter(nextTpsPaymentItem => nextTpsPaymentItem.paymentItemId.contains(chargeRefNotificationPciPalRequest.paymentItemId))
    val tpsPaymentsListNew = update.headOption match {
      case Some(singleUpdate) =>
        val updated = singleUpdate.copy(pcipalData = Some(chargeRefNotificationPciPalRequest))
        remainder.::(updated)
      case None => throw new IdNotFoundException(s"Could not find paymentItemId: ${chargeRefNotificationPciPalRequest.paymentItemId.value}")
    }

    tpsPayments.copy(payments = tpsPaymentsListNew)
  }
}
