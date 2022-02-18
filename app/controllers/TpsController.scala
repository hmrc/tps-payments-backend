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
import repository.{EmailCrypto, TpsPaymentsRepo}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import scala.util.{Failure, Success}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TpsController @Inject() (actions:        Actions,
                               cc:             ControllerComponents,
                               tpsRepo:        TpsPaymentsRepo,
                               emailConnector: EmailConnector,
                               emailCrypto:    EmailCrypto)(implicit executionContext: ExecutionContext) extends BackendController(cc) {

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
      case Some(email) => tpsPaymentItem.copy(email = Some(emailCrypto.encryptEmailIfNotAlreadyEncrypted(email)))
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
    logger.info(s"getPaymentItem ${id.value}")

    tpsRepo.findPaymentItem(id).map {
      case Some(paymentItem) =>
        logger.info("taxType found:" + paymentItem.taxType.toString)
        Ok(toJson(paymentItem.taxType))
      case None              =>
        logger.info("taxType not found")
        NotFound(s"No payment item found for id ${id.value}")
    }
  }

  def delete(tpsId: TpsId): Action[AnyContent] = actions.strideAuthenticateAction().async {
    logger.debug(s"delete, id= ${tpsId.value}")
    tpsRepo.removeById(tpsId).map(_ => Ok)
  }

  def updateWithPcipalSessionId(): Action[UpdateRequest] = actions.strideAuthenticateAction().async(parse.json[UpdateRequest]) { implicit request =>
    logger.info(s"updateWithPcipalSessionId, update= ${request.body.toString}")
    for {
      record <- tpsRepo.getPayment(request.body.tpsId)
      newRecord = record.copy(pciPalSessionId = Some(request.body.pcipalSessionId))
      _ <- tpsRepo.upsert(request.body.tpsId, newRecord)
    } yield {
      Ok
    }
  }

  def updateWithPcipalData(): Action[ChargeRefNotificationPcipalRequest] = Action.async(parse.json[ChargeRefNotificationPcipalRequest]) { implicit request =>
    logger.info(s"updateWithPcipalData, update= ${request.body.toString}")

    val f = for {
      existingTpsPayments <- tpsRepo.findByPcipalSessionId(request.body.PCIPalSessionId)
      updatedTpsPayments = updateTpsPayments(existingTpsPayments, request.body)
      _ <- tpsRepo.upsert(updatedTpsPayments._id, updatedTpsPayments)
      _ = if(tpsPaymentsAreFullyUpdated(updatedTpsPayments)) maybeSendEmail(updatedTpsPayments)
    } yield Ok

    f.recover {
      case e: IdNotFoundException => BadRequest(e.getMessage)
    }
  }

  private def updateTpsPayments(tpsPayments: TpsPayments, chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest): TpsPayments = {
    logger.debug("updateTpsPayments")
    val remainder: List[TpsPaymentItem] = tpsPayments.payments.filterNot(nextTpsPaymentItem => nextTpsPaymentItem.paymentItemId.contains(chargeRefNotificationPciPalRequest.PaymentItemId))
    val update: List[TpsPaymentItem] = tpsPayments.payments.filter(nextTpsPaymentItem => nextTpsPaymentItem.paymentItemId.contains(chargeRefNotificationPciPalRequest.PaymentItemId))

    val tpsPaymentsListNew = update.headOption match {
      case Some(singleUpdate) =>
        val updated = singleUpdate.copy(pcipalData = Some(chargeRefNotificationPciPalRequest))
        remainder.::(updated)
      case None => throw new IdNotFoundException(s"Could not find paymentItemId: ${chargeRefNotificationPciPalRequest.PaymentItemId.value}")
    }

    tpsPayments.copy(payments = tpsPaymentsListNew)
  }
 
  private def maybeSendEmail(tpsPayments: TpsPayments)(implicit hc: HeaderCarrier): Unit = {
    logger.info("maybeSendEmail")
      val listOfSuccessfulTpsPaymentItems: List[TpsPaymentItem] =
        tpsPayments.payments.filter(nextTpsPaymentItem => nextTpsPaymentItem.pcipalData
          .fold(throw new RuntimeException("maybeSendEmail error: payment status should be present but isn't")) (nextPaymentItemPciPalData => nextPaymentItemPciPalData.Status.equals(StatusTypes.validated)))

      if (listOfSuccessfulTpsPaymentItems.isEmpty) ()
      else {
        tpsPayments.payments.find(paymentItem => paymentItem.email.nonEmpty) match {
          case Some(TpsPaymentItem(_, _, _, _, _, _, Some(pcipalData), _, _, Some(email), _)) =>
            sendEmail(listOfSuccessfulTpsPaymentItems, pcipalData.ReferenceNumber.dropRight(2), email, pcipalData.CardType, pcipalData.CardLast4)
          case _ => throw new RuntimeException("maybeSendEmail error: data which should be present are missing")
        }
      }
  }

  private def sendEmail(tpsPaymentItems: List[TpsPaymentItem], transactionReference: String, emailAddress: String, cardType: String, cardNumber: String)(implicit hc: HeaderCarrier): Unit = {
    logger.info(s"sendEmail emailAddressEncrypt:$emailAddress emailAddress:${emailCrypto.decryptEmail(emailAddress)} " +
      s"totalAmountpaid${tpsPaymentItems.map(tpsPaymentItem => tpsPaymentItem.amount).sum.setScale(2).toString} transactionReference: " +
      s"$transactionReference tpsPaymentItemsForEmail: ${parseTpsPaymentsItemsForEmail(tpsPaymentItems)} cardType: $cardType cardNumber: $cardNumber")

    emailConnector.sendEmail(
      emailAddress            = emailCrypto.decryptEmail(emailAddress),
      totalAmountPaid         = parseBigDecimalToString(tpsPaymentItems.map(tpsPaymentItem => tpsPaymentItem.amount).sum),
      transactionReference    = transactionReference,
      cardType                = cardType,
      cardNumber              = cardNumber,
      tpsPaymentItemsForEmail = parseTpsPaymentsItemsForEmail(tpsPaymentItems)
    )
    ()
  }

  private def tpsPaymentsAreFullyUpdated(tpsPayments: TpsPayments): Boolean = {
    logger.info(s"tpsPaymentsAreFullyUpdated: ${tpsPayments.payments.forall(nextTpsPaymentItem => nextTpsPaymentItem.pcipalData.nonEmpty)}")
    tpsPayments.payments.forall(nextTpsPaymentItem => nextTpsPaymentItem.pcipalData.nonEmpty)
  }

  def parseTpsPaymentsItemsForEmail(tpsPayments: List[TpsPaymentItem]): String = {
    JsArray(tpsPayments.map(nextPaymentItem =>
      toJson(TpsPaymentItemForEmail(
        taxType           = getTaxTypeString(nextPaymentItem.taxType),
        amount            = parseBigDecimalToString(nextPaymentItem.amount),
        transactionFee    = nextPaymentItem.pcipalData.fold("Unknown")(pciPalData => parseBigDecimalToString(pciPalData.Commission)),
        transactionNumber = nextPaymentItem.paymentItemId.fold("Unknown")(paymentItemId => paymentItemId.value)
      )))).toString
  }
  
  private def parseBigDecimalToString(bigDecimal: BigDecimal): String = {
    bigDecimal.setScale(2).toString
  }
  
  private def getTaxTypeString(taxType: TaxType): String = taxType match {
    case TaxTypes.ChildBenefitsRepayments => "Child Benefits repayments"
    case TaxTypes.Sa                      => "Self Assessment"
    case TaxTypes.Sdlt                    => "Stamp Duty Land Tax"
    case TaxTypes.Safe                    => "SAFE"
    case TaxTypes.Cotax                   => "Corporation Tax"
    case TaxTypes.Ntc                     => "Tax credit repayments"
    case TaxTypes.Paye                    => "PAYE"
    case TaxTypes.Nps                     => "NPS/NIRS"
    case TaxTypes.Vat                     => "VAT"
    case _                                => throw new RuntimeException("getTaxTypeString Error: Unknown TaxType")
  }
}
