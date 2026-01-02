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

package email

import email.model.IndividualPaymentForEmail
import testsupport.ItSpec
import tps.model._
import tps.testdata.TdAll

class EmailServiceSpec extends ItSpec:

  def emailService: EmailService = app.injector.instanceOf[EmailService]

  "parseTpsPaymentsItemsForEmail should default transactionFee and transactionNumber to 'Unknown' if pcipalData is None" in {
    @SuppressWarnings(Array("org.wartremover.warts.IterableOps"))
    val testTpsPaymentItemWithNoPciPalData: PaymentItem      = TdAll.TdJourneySa.journeyAtPciPal.payments.head
    val individualPaymentForEmail: IndividualPaymentForEmail =
      emailService.toIndividualPaymentForEmail(testTpsPaymentItemWithNoPciPalData)
    individualPaymentForEmail.transactionFee shouldBe "Unknown"
    individualPaymentForEmail.transactionNumber shouldBe "Unknown"
  }

  Seq[(TaxType, String)](
    TaxTypes.ChildBenefitsRepayments -> "Child Benefits repayments",
    TaxTypes.Sa                      -> "Self Assessment",
    TaxTypes.Sdlt                    -> "Stamp Duty Land Tax",
    TaxTypes.Safe                    -> "SAFE",
    TaxTypes.Cotax                   -> "Corporation Tax",
    TaxTypes.Ntc                     -> "Tax credit repayments",
    TaxTypes.Paye                    -> "PAYE",
    TaxTypes.Nps                     -> "NPS/NIRS",
    TaxTypes.Vat                     -> "VAT",
    TaxTypes.Ppt                     -> "Plastic Packaging Tax",
    TaxTypes.MIB                     -> "MIB",
    TaxTypes.PNGR                    -> "PNGR"
  ).foreach { case (tt, expectedTaxTypeString) =>
    s"parseTpsPaymentsItemsForEmail should use getTaxTypeString to derive correct string: [${tt.entryName}]" in {
      // TODO: stronger test would compare entire object instead of just one filed.
      // TODO: rewrite this so it iterates through valid test data instead of mocking only tax type
      @SuppressWarnings(Array("org.wartremover.warts.IterableOps"))
      val testTpsPaymentItem                                   = TdAll.TdJourneySa.journeyReceivedNotification.payments.head
      val tpsPaymentItem                                       = testTpsPaymentItem.copy(taxType = tt)
      val individualPaymentForEmail: IndividualPaymentForEmail =
        emailService.toIndividualPaymentForEmail(tpsPaymentItem)
      individualPaymentForEmail.taxType shouldBe expectedTaxTypeString
    }
  }
