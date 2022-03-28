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

package services

import connectors.EmailConnector
import model.TaxTypes.{MIB, PNGR}
import model.pcipal.ChargeRefNotificationPcipalRequest
import model.{IndividualPaymentForEmail, StatusTypes, TaxType, TaxTypes, TpsPaymentItem}
import play.api.libs.json.JsArray
import play.api.libs.json.Json.toJson
import repository.EmailCrypto
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}

@Singleton
class EmailService @Inject() (emailCrypto:    EmailCrypto,
                              emailConnector: EmailConnector) {

  def maybeSendEmail(tpsPaymentItems: List[TpsPaymentItem])(implicit hc: HeaderCarrier): Unit = {
    if (weShouldSendEmail(tpsPaymentItems)) {
      val emailAddress: String = tpsPaymentItems.find(_.email.nonEmpty).flatMap(_.email).fold("impossible")(email => email)
      val listOfSuccessfulTpsPaymentItems: List[TpsPaymentItem] =
        tpsPaymentItems.filter(_.pcipalData
          .fold(throw new RuntimeException("maybeSendEmail error: pcipal data should be present but isn't")) (nextPaymentItemPciPalData => nextPaymentItemPciPalData.Status.equals(StatusTypes.validated)))

      listOfSuccessfulTpsPaymentItems.headOption match {
        case Some(TpsPaymentItem(_, _, _, _, _, _, Some(ChargeRefNotificationPcipalRequest(_, _, _, _, cardType, _, _, _, _, _, referenceNumber, cardLast4)), _, _, _)) =>
          sendEmail(listOfSuccessfulTpsPaymentItems, referenceNumber.dropRight(2), emailAddress, cardType, cardLast4)
        case _ => ()
      }
    } else ()
  }

  private def sendEmail(tpsPaymentItems: List[TpsPaymentItem], transactionReference: String, emailAddress: String, cardType: String, cardNumber: String)(implicit hc: HeaderCarrier): Unit = {
    val totalCommissionPaid: BigDecimal = tpsPaymentItems.map(nextTpsPaymentItem => nextTpsPaymentItem.pcipalData.fold(BigDecimal(0))(pcipalData => pcipalData.Commission)).sum
    val totalAmountPaid: BigDecimal = tpsPaymentItems.map(nextTpsPaymentItem => nextTpsPaymentItem.amount).sum

    emailConnector.sendEmail(
      emailAddress            = emailCrypto.decryptEmail(emailAddress),
      totalAmountPaid         = parseBigDecimalToString(totalCommissionPaid + totalAmountPaid),
      transactionReference    = transactionReference,
      cardType                = cardType,
      cardNumber              = cardNumber,
      tpsPaymentItemsForEmail = stringifyTpsPaymentsItemsForEmail(parseTpsPaymentsItemsForEmail(tpsPaymentItems))
    )
    ()
  }

  private def weShouldSendEmail(tpsPaymentItems: List[TpsPaymentItem]): Boolean = {
    isNotMibOrPngr(tpsPaymentItems) || tpsPaymentsAreFullyUpdated(tpsPaymentItems) || emailAddressHasBeenProvided(tpsPaymentItems)
  }

  private def tpsPaymentsAreFullyUpdated(tpsPaymentItems: List[TpsPaymentItem]): Boolean = tpsPaymentItems.forall(_.pcipalData.nonEmpty)

  private def emailAddressHasBeenProvided(tpsPaymentItems: List[TpsPaymentItem]): Boolean = tpsPaymentItems.exists(_.email.nonEmpty)

  private def isNotMibOrPngr(tpsPaymentItems: List[TpsPaymentItem]): Boolean = {
    !tpsPaymentItems.exists(nextPaymentItem => nextPaymentItem.taxType.equals(MIB) || nextPaymentItem.taxType.equals(PNGR))
  }

  def parseTpsPaymentsItemsForEmail(tpsPayments: List[TpsPaymentItem]): List[IndividualPaymentForEmail] = {
    tpsPayments.map(nextPaymentItem =>
      IndividualPaymentForEmail(
        taxType           = getTaxTypeString(nextPaymentItem.taxType),
        amount            = parseBigDecimalToString(nextPaymentItem.amount),
        transactionFee    = nextPaymentItem.pcipalData.fold("Unknown")(pcipalData => parseBigDecimalToString(pcipalData.Commission)),
        transactionNumber = nextPaymentItem.pcipalData.fold("Unknown")(pcipalData => pcipalData.ReferenceNumber)
      ))
  }

  def stringifyTpsPaymentsItemsForEmail(tpsPaymentsForEmail: List[IndividualPaymentForEmail]): String = JsArray(tpsPaymentsForEmail.map(toJson(_))).toString

  private def parseBigDecimalToString(bigDecimal: BigDecimal): String = bigDecimal.setScale(2).toString

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
    case TaxTypes.P800                    => taxType.toString
    case TaxTypes.MIB                     => taxType.toString
    case TaxTypes.PNGR                    => taxType.toString
  }
}
