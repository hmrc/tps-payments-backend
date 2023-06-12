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
import repository.JourneyRepo
import services.EmailService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import util.EmailCrypto

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TpsController @Inject() (actions:      Actions,
                               cc:           ControllerComponents,
                               tpsRepo:      JourneyRepo,
                               emailService: EmailService,
                               emailCrypto:  EmailCrypto)(implicit executionContext: ExecutionContext) extends BackendController(cc) {

  val logger: Logger = Logger(this.getClass)

  def startTpsJourneyMibOrPngr: Action[TpsPaymentRequest] = actions.strideAuthenticateAction().async(parse.json[TpsPaymentRequest]) { implicit request =>
    val tpsPayments: Journey = encryptEmail(request.body.tpsPayments(Instant.now()))
    tpsRepo.upsert(tpsPayments).map { _ =>
      Created(toJson(tpsPayments._id))
    }
  }

  def upsert(): Action[Journey] = actions.strideAuthenticateAction().async(parse.json[Journey]) { implicit request =>
    val tpsPayments: Journey = encryptEmail(request.body)
    tpsRepo.upsert(tpsPayments).map { _ =>
      Ok(toJson(request.body._id))
    }
  }

  private def encryptEmail(tpsPayments: Journey): Journey = {
    val paymentItems: List[TpsPaymentItem] = tpsPayments.payments
    val paymentItemsWithEncryptedEmails: List[TpsPaymentItem] = paymentItems.map(tpsPaymentItem => tpsPaymentItem.email match {
      case Some(email) => tpsPaymentItem.copy(email = Some(emailCrypto.encryptEmailIfNotAlreadyEncrypted(email)))
      case _           => tpsPaymentItem
    })
    tpsPayments.copy(payments = paymentItemsWithEncryptedEmails)
  }

  def findTpsPayments(id: JourneyId): Action[AnyContent] = actions.strideAuthenticateAction().async {
    tpsRepo.findPayment(id).map {
      case Some(tpsPayments) =>
        val tpsPaymentItemsWithDecryptedEmail: List[TpsPaymentItem] = tpsPayments
          .payments
          .map(nextTpsPaymentItem =>
            nextTpsPaymentItem.copy(
              email = emailCrypto.maybeDecryptEmail(nextTpsPaymentItem.email)
            )
          )

        val tpsPaymentsWithDecryptedEmails: Journey = tpsPayments.copy(payments = tpsPaymentItemsWithDecryptedEmail)
        Ok(toJson(tpsPaymentsWithDecryptedEmails))
      case None => NotFound(s"No payments found for id ${id.value}")
    }
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

  private def updateTpsPayments(tpsPayments: Journey, chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest): Journey = {
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
