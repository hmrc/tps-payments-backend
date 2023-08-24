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

package tps.testdata

import tps.journey.model.JourneyId
import tps.model._
import tps.pcipalmodel._

import java.time.Instant

trait TdJourneyVat { dependencies: TdBase =>

  object TdJourneyVat extends TdJourneyInStates {

    override lazy val journeyId: JourneyId = dependencies.journeyId
    override lazy val pid: String = dependencies.pid
    override lazy val created: Instant = dependencies.instant
    override lazy val navigation: Navigation = dependencies.navigation
    override lazy val amountString: String = "111.11"
    override lazy val taxReference: String = "968501144"
    override final val selectedTaxType: TaxType = TaxTypes.VAT

    override lazy val paymentSpecificData: VatSpecificData = VatSpecificData(
      vatReference   = taxReference,
      remittanceType = "Unaccompanied" //0 PciPal Value
    )

    override lazy val pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId              = dependencies.flowId,
      InitialValues       = List(PcipalInitialValues(
        clientId           = "VAPM",
        pid                = dependencies.pid,
        accountOfficeId    = "G1",
        HODIdentifier      = HeadOfDutyIndicators.V,
        UTRReference       = taxReference,
        name1              = dependencies.customerName.value,
        amount             = amountString,
        taxAmount          = None,
        nicAmount          = None,
        lnpClass2          = None,
        nirRate            = None,
        startDate          = None,
        endDate            = None,
        vatPeriodReference = None,
        vatRemittanceType  = Some("0"),
        paymentItemId      = dependencies.paymentItemId,
        chargeReference    = taxReference,
        taxRegimeDisplay   = "VAT",
        reference          = dependencies.pciPalReferenceNumber,
        increment          = "1"
      )),
      UTRBlacklistFlag    = "N",
      postcodeFlag        = "Y",
      taxRegime           = "gen",
      TotalTaxAmountToPay = amountString,
      callbackUrl         = navigation.callback,
      backUrl             = navigation.back,
      resetUrl            = navigation.reset,
      finishUrl           = navigation.finish
    )

    override lazy val pcipalSessionLaunchResponse: PcipalSessionLaunchResponse = PcipalSessionLaunchResponse(
      Id     = dependencies.pciPalSessionId,
      LinkId = dependencies.linkId
    )

    override lazy val pcipalData: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
      HoD                  = HeadOfDutyIndicators.V,
      TaxReference         = taxReference,
      Amount               = amount,
      Commission           = 0,
      CardType             = dependencies.cardType,
      Status               = StatusTypes.validated,
      PCIPalSessionId      = dependencies.pciPalSessionId,
      TransactionReference = taxReference,
      paymentItemId        = dependencies.paymentItemId,
      ChargeReference      = taxReference,
      ReferenceNumber      = dependencies.pciPalReferenceNumber,
      CardLast4            = dependencies.cardLast4Digits
    )

    override lazy val paymentItemBeforePcipal: PaymentItem = PaymentItem(
      paymentItemId       = dependencies.paymentItemId,
      amount              = amount,
      headOfDutyIndicator = HeadOfDutyIndicators.V,
      updated             = dependencies.instant,
      customerName        = dependencies.customerName,
      chargeReference     = taxReference,
      pcipalData          = None,
      paymentSpecificData = paymentSpecificData,
      taxType             = TaxTypes.VAT,
      email               = Some(dependencies.email)
    )

    override lazy val paymentItem: PaymentItem = paymentItemBeforePcipal.copy(pcipalData = Some(pcipalData))

    override lazy val journeyCreatedJson: JourneyJson = JourneyJson(
      "/tps/testdata/vat/journey-1-Created.json"
    )

    override lazy val journeySelectedTaxTypeJson: JourneyJson = JourneyJson(
      "/tps/testdata/vat/journey-2-SelectedTaxType.json"
    )

    override lazy val journeyEnteredPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vat/journey-3-EnteredPayment.json"
    )

    override lazy val journeyAtPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vat/journey-4-AtPciPal.json"
    )

    override lazy val journeyResetByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vat/journey-5-ResetByPciPal.json"
    )

    override lazy val journeyFinishedByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vat/journey-6-FinishedByPciPal.json"
    )

    override lazy val journeyBackByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vat/journey-7-BackByPciPal.json"
    )

    override lazy val journeyReceivedNotificationJson: JourneyJson = JourneyJson(
      "/tps/testdata/vat/journey-8-ReceivedNotification.json"
    )
  }

}
