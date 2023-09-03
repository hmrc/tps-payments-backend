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

import tps.journey.model.{Journey, JourneyId, JourneyState}
import tps.model._
import tps.pcipalmodel._
import tps.startjourneymodel.{SjPaymentItem, StartJourneyRequestMibOrPngr}

import java.time.Instant

trait TdJourneyMib { dependencies: TdBase =>

  object TdJourneyMib extends TdJourneyInStatesExternalTaxTypes {

    override final val taxType: ExternalTaxType = TaxTypes.MIB

    override lazy val startJourneyRequest: StartJourneyRequestMibOrPngr = StartJourneyRequestMibOrPngr(
      pid        = dependencies.pid,
      payments   = Seq[SjPaymentItem](
        SjPaymentItem(
          chargeReference     = taxReference,
          customerName        = dependencies.customerName,
          amount              = amount,
          taxRegimeDisplay    = "MODS", //https://github.com/hmrc/merchandise-in-baggage-frontend/blob/96027c5b4cdbc4f957c06b0c7e295861962f2432/app/uk/gov/hmrc/merchandiseinbaggage/model/api/tpspayments/TpsPaymentsItem.scala#L25
          taxType             = taxType,
          paymentSpecificData = paymentSpecificData,
          email               = Some(dependencies.email)
        )
      ),
      navigation = dependencies.navigation
    )

    override lazy val journeyId: JourneyId = dependencies.journeyId
    override lazy val pid: String = dependencies.pid
    override lazy val created: Instant = dependencies.instant
    override lazy val navigation: Navigation = dependencies.navigation
    override lazy val amountString: String = "102.02"
    override lazy val taxReference: String = "XJPR5573376231"

    override lazy val paymentSpecificData: MibSpecificData = MibSpecificData(
      chargeReference    = taxReference,
      vat                = BigDecimal("0.07"),
      customs            = BigDecimal("0.05"),
      amendmentReference = Some(123)
    )

    override lazy val pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId              = dependencies.flowId,
      InitialValues       = List(PcipalInitialValues(
        clientId           = "MBML",
        pid                = dependencies.pid,
        accountOfficeId    = "S1",
        HODIdentifier      = HeadOfDutyIndicators.B,
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
        vatRemittanceType  = None,
        paymentItemId      = dependencies.paymentItemId,
        chargeReference    = taxReference,
        taxRegimeDisplay   = "MIB",
        reference          = dependencies.pciPalReferenceNumber,
        increment          = "1"
      )),
      UTRBlacklistFlag    = "N",
      postcodeFlag        = "Y",
      taxRegime           = "gbl",
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
      HoD                  = HeadOfDutyIndicators.B,
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
      headOfDutyIndicator = HeadOfDutyIndicators.B,
      updated             = instant,
      customerName        = dependencies.customerName,
      chargeReference     = taxReference,
      pcipalData          = None,
      paymentSpecificData = paymentSpecificData,
      taxType             = TaxTypes.MIB,
      email               = Some(dependencies.email)
    )

    override lazy val paymentItem: PaymentItem = paymentItemBeforePcipal.copy(pcipalData = Some(pcipalData))

    override lazy val journeyCreated: Journey = Journey(
      _id                         = journeyId,
      journeyState                = JourneyState.Started,
      pid                         = pid,
      created                     = created,
      payments                    = List(paymentItemBeforePcipal),
      navigation                  = navigation,
      pcipalSessionLaunchRequest  = None,
      pcipalSessionLaunchResponse = None
    )

    override lazy val journeyCreatedJson: JourneyJson = JourneyJson(
      "/tps/testdata/mib/journey-1-Created.json"
    )

    override lazy val journeyAtPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/mib/journey-4-AtPciPal.json"
    )

    override lazy val journeyResetByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/mib/journey-5-ResetByPciPal.json"
    )

    override lazy val journeyFinishedByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/mib/journey-6-FinishedByPciPal.json"
    )

    override lazy val journeyBackByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/mib/journey-7-BackByPciPal.json"
    )

    override lazy val journeyReceivedNotificationJson: JourneyJson = JourneyJson(
      "/tps/testdata/mib/journey-8-ReceivedNotification.json"
    )

  }

}
