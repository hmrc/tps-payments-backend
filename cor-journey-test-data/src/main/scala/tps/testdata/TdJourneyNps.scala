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

trait TdJourneyNps { dependencies: TdBase =>

  object TdJourneyNps extends TdJourneyInStates {

    override lazy val journeyId: JourneyId = dependencies.journeyId
    override lazy val pid: String = dependencies.pid
    override lazy val created: Instant = dependencies.instant
    override lazy val navigation: Navigation = dependencies.navigation
    override lazy val amountString: String = "110.01"
    override lazy val taxReference: String = "AA000000JM"
    override final val selectedTaxType: TaxType = TaxTypes.NPS

    override lazy val paymentSpecificData: NpsSpecificData = NpsSpecificData(
      npsReference    = taxReference,
      periodStartDate = "020122",
      periodEndDate   = "080122",
      npsType         = "Class 2 National Insurance",
      rate            = 1
    )

    override lazy val pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId              = dependencies.flowId,
      InitialValues       = List(PcipalInitialValues(
        clientId           = "NPPL",
        pid                = dependencies.pid,
        accountOfficeId    = "G1",
        HODIdentifier      = HeadOfDutyIndicators.J,
        UTRReference       = taxReference,
        name1              = dependencies.customerName.value,
        amount             = amountString,
        taxAmount          = None,
        nicAmount          = None,
        lnpClass2          = Some("61"),
        nirRate            = Some("1.00"),
        startDate          = Some("020122"),
        endDate            = Some("080122"),
        vatPeriodReference = None,
        vatRemittanceType  = None,
        paymentItemId      = dependencies.paymentItemId,
        chargeReference    = taxReference,
        taxRegimeDisplay   = "NPS",
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
      HoD                  = HeadOfDutyIndicators.J,
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
      headOfDutyIndicator = HeadOfDutyIndicators.J,
      updated             = dependencies.instant,
      customerName        = dependencies.customerName,
      chargeReference     = taxReference,
      pcipalData          = None,
      paymentSpecificData = paymentSpecificData,
      taxType             = TaxTypes.NPS,
      email               = Some(dependencies.email)
    )

    override lazy val paymentItem: PaymentItem = paymentItemBeforePcipal.copy(pcipalData = Some(pcipalData))

    override lazy val journeyCreatedJson = JourneyJson(
      "/tps/testdata/nps/journey-1-Created.json"
    )

    override lazy val journeySelectedTaxTypeJson: JourneyJson = JourneyJson(
      "/tps/testdata/nps/journey-2-SelectedTaxType.json"
    )

    override lazy val journeyEnteredPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/nps/journey-3-EnteredPayment.json"
    )

    override lazy val journeyAtPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/nps/journey-4-AtPciPal.json"
    )

    override lazy val journeyResetByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/nps/journey-5-ResetByPciPal.json"
    )

    override lazy val journeyFinishedByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/nps/journey-6-FinishedByPciPal.json"
    )

    override lazy val journeyBackByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/nps/journey-7-BackByPciPal.json"
    )

    override lazy val journeyReceivedNotificationJson: JourneyJson = JourneyJson(
      "/tps/testdata/nps/journey-8-ReceivedNotification.json"
    )
  }

}
