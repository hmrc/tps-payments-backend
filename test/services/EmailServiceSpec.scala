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

package services

import config.AppConfig
import connectors.EmailConnector
import model.TaxTypes.P800
import model._
import support.ItSpec
import support.testdata.TestData.paymentItemId
import uk.gov.hmrc.http.HttpClient
import util.EmailCrypto

import java.time.Instant

class EmailServiceSpec extends ItSpec {

  "EmailService" - {

    val emailCrypto = new EmailCrypto("MWJhcmNsYXlzc2Z0cGRldg==")
    val emailConnector = new EmailConnector(app.injector.instanceOf[HttpClient], app.injector.instanceOf[AppConfig])(ec)
    val service = new EmailService(emailCrypto, emailConnector)

    val testTpsPaymentItem = TpsPaymentItem(Some(paymentItemId), 1.92, HeadOfDutyIndicators.B, Instant.parse("2020-01-20T11:56:46Z"), "JB", "12345", None, PaymentSpecificDataP800("JE231111", "B", "P800", 2000), P800, Some("test@email.com"))

    "parseTpsPaymentsItemsForEmail should default transactionFee and transactionNumber to 'Unknown' if pcipalData is None" in {
      val testTpsPaymentItemWithNoPciPalData = testTpsPaymentItem
      val individualPaymentForEmail = service.parseTpsPaymentsItemsForEmail(List(testTpsPaymentItemWithNoPciPalData))
      individualPaymentForEmail.headOption.value.transactionFee shouldBe "Unknown"
      individualPaymentForEmail.headOption.value.transactionNumber shouldBe "Unknown"
    }

    Seq[(TaxType, String)](
      TaxTypes.ChildBenefitsRepayments -> "Child Benefits repayments",
      TaxTypes.Sa -> "Self Assessment",
      TaxTypes.Sdlt -> "Stamp Duty Land Tax",
      TaxTypes.Safe -> "SAFE",
      TaxTypes.Cotax -> "Corporation Tax",
      TaxTypes.Ntc -> "Tax credit repayments",
      TaxTypes.Paye -> "PAYE",
      TaxTypes.Nps -> "NPS/NIRS",
      TaxTypes.Vat -> "VAT",
      TaxTypes.Ppt -> "Plastic Packaging Tax",
      TaxTypes.P800 -> "P800",
      TaxTypes.MIB -> "MIB",
      TaxTypes.PNGR -> "PNGR"
    ).foreach {
        case (tt, expectedTaxTypeString) =>
          s"parseTpsPaymentsItemsForEmail should use getTaxTypeString to derive correct string: [${tt.entryName}]" in {
            val tpsPaymentItem = testTpsPaymentItem.copy(taxType = tt)
            val individualPaymentForEmail = service.parseTpsPaymentsItemsForEmail(List(tpsPaymentItem))
            individualPaymentForEmail.headOption.value.taxType shouldBe expectedTaxTypeString
          }
      }
  }
}
