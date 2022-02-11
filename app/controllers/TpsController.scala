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

import java.time.LocalDateTime

import auth.Actions
import connectors.EmailConnector
import javax.inject.{Inject, Singleton}
import model._
import model.pcipal.ChargeRefNotificationPcipalRequest
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.libs.json.JsArray
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repository.{Crypto, TpsPaymentsRepo}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import scala.util.{Failure, Success}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TpsController @Inject() (actions:        Actions,
                               cc:             ControllerComponents,
                               tpsRepo:        TpsPaymentsRepo,
                               emailConnector: EmailConnector,
                               crypto:         Crypto)(implicit executionContext: ExecutionContext) extends BackendController(cc) {

  val logger: Logger = Logger(this.getClass)

  val createTpsPayments: Action[TpsPaymentRequest] = actions.strideAuthenticateAction().async(parse.json[TpsPaymentRequest]) { implicit request =>
    val tpsPayments = request.body.tpsPayments(LocalDateTime.now())

    tpsRepo.upsert(tpsPayments._id, tpsPayments).map { _ =>
      Created(toJson(tpsPayments._id))
    }
  }

  def storeTpsPayments(): Action[TpsPayments] = actions.strideAuthenticateAction().async(parse.json[TpsPayments]) { implicit request =>
    val updatedPayments = request.body.payments map (payment => payment.copy(paymentItemId = Some(PaymentItemId.fresh)))

    val updatedPaymentsWithEncryptedEmails = updatedPayments.map(tpsPaymentItem => tpsPaymentItem.email match {
      case Some(email) => tpsPaymentItem.copy(email = Some(crypto.encrypt(email)))
      case _           => tpsPaymentItem
    })

    tpsRepo.upsert(request.body._id, request.body.copy(payments = updatedPaymentsWithEncryptedEmails)).map { _ =>
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
      _ = maybeSendEmail(a, request.body.Status)
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

  private def maybeSendEmail(tpsPayments: TpsPayments, status: model.StatusType)(implicit hc: HeaderCarrier): Unit = {
    logger.debug("maybeSendEmail")
    val tuple = (tpsPayments.payments.find(paymentItem => paymentItem.email.nonEmpty), status)
    tuple match {
      case (Some(paymentItemWithEmail), StatusTypes.validated) => sendEmail(
        tpsPayments,
        paymentItemWithEmail.languageCode.getOrElse("en"),
        paymentItemWithEmail.email.getOrElse(throw new RuntimeException("maybeSendEmail error: email should be present but isn't")))
      case _ => ()
    }
  }

  private def sendEmail(tpsPayments: TpsPayments, languageCode: String, emailAddress: String)(implicit hc: HeaderCarrier): Unit = {
    logger.debug("sendEmail")

    emailConnector.sendEmail(
      languageCode            = languageCode,
      emailAddress            = decryptEmail(emailAddress),
      totalAmountPaid         = tpsPayments.payments.map(tpsPaymentItem => tpsPaymentItem.amount).sum.setScale(2).toString,
      transactionReference    = tpsPayments._id.value,
      tpsPaymentItemsForEmail = parseTpsPaymentsItemsForEmail(tpsPayments).toString
    )
    ()
  }

  def parseTpsPaymentsItemsForEmail(tpsPayments: TpsPayments): JsArray = {
    JsArray(tpsPayments.payments.map(nextPaymentItem =>
      toJson(TpsPaymentItemForEmail(
        taxType           = getTaxTypeString(nextPaymentItem.taxType),
        amount            = nextPaymentItem.amount.setScale(2).toString,
        transactionNumber = nextPaymentItem.paymentItemId.fold("n/a")(paymentItemId => paymentItemId.value)
      ))))
  }
  
  private def getTaxTypeString(taxType: TaxType): String = taxType match {
    case TaxTypes.ChildBenefitsRepayments => "Child Benefits repayments"
    case TaxTypes.Sa                      => "Self Assessment"
    case TaxTypes.Sdlt                    => "Stamp Duty Land Tax"
    case TaxTypes.Safe                    => "SAFE"
    case TaxTypes.Cotax                   => "Corporation Tax"
    case TaxTypes.Ntc                     => "Tax credit repayments"
    case TaxTypes.Paye                    => "PAYE"
    case TaxTypes.Nps                     => "NPS"
    case TaxTypes.Vat                     => "VAT"
    case _                                => taxType.toString
  }

  private def decryptFailureException(ex: Throwable, field: String) = throw new RuntimeException(s"Failed to decrypt field $field due to exception ${ex.getMessage}")

  private def decryptEmail(email: String): String = crypto.decrypt(email) match {
    case Failure(ex)    => decryptFailureException(ex, "email")
    case Success(value) => value
  }
}
