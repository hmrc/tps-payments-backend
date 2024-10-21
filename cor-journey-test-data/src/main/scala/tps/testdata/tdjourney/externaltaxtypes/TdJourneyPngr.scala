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

package tps.testdata.tdjourney.externaltaxtypes

import play.api.libs.json.JsObject
import tps.journey.model.{Journey, JourneyId, JourneyState, StartJourneyResponse}
import tps.model._
import tps.pcipalmodel._
import tps.startjourneymodel.StartJourneyRequestPngr
import tps.testdata.TdBase
import tps.testdata.util.JsonSyntax.toJsonOps
import tps.testdata.util.{JourneyJson, ResourceReader}

import java.time.Instant

trait TdJourneyPngr { dependencies: TdBase =>

  object TdJourneyPngr extends TdJourneyInStatesExternalTaxTypes {

    override final val taxType: ExternalTaxType = TaxTypes.PNGR

    lazy val startJourneyRequest: StartJourneyRequestPngr = StartJourneyRequestPngr(
      chargeReference = taxReference,
      customerName    = dependencies.customerName,
      amount          = amount,
      backUrl         = dependencies.navigation.back,
      resetUrl        = dependencies.navigation.reset,
      finishUrl       = dependencies.navigation.finish
    )

    lazy val startJourneyRequestJson: JsObject = ResourceReader.read("/tps/testdata/pngr/0-startJourneyRequest.json").asJson
    lazy val startJourneyRequestJsonAmountsAsStrings: JsObject = ResourceReader.read("/tps/testdata/pngr/0-startJourneyRequest-amounts-as-strings.json").asJson

    lazy val startJourneyResponse: StartJourneyResponse = StartJourneyResponse(
      journeyId = dependencies.journeyId, nextUrl = s"http://localhost:9124/tps-payments/make-payment/pngr/${dependencies.journeyId.value}"
    )
    override lazy val journeyId: JourneyId = dependencies.journeyId
    override lazy val pid: String = dependencies.pid
    override lazy val created: Instant = dependencies.instant
    override lazy val navigation: Navigation = dependencies.navigation
    override lazy val amountString: String = "101.01"
    override lazy val taxReference: String = "XSPR5814332193"

    override lazy val paymentSpecificData: PngrSpecificData = PngrSpecificData(
      chargeReference = taxReference
    )

    override lazy val pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId              = dependencies.flowId,
      InitialValues       = List(PcipalInitialValues(
        clientId        = "PSML",
        pid             = dependencies.pid,
        accountOfficeId = "S1",
        HODIdentifier   = HeadOfDutyIndicators.B,
        UTRReference    = taxReference,
        name1           = dependencies.customerName.value,
        amount          = amountString,

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
        taxRegimeDisplay   = "PNGR",
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
      finishUrl           = navigation.finish,
      LanguageFlag        = "E"
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
      updated             = dependencies.instant,
      customerName        = dependencies.customerName,
      chargeReference     = taxReference,
      pcipalData          = None,
      paymentSpecificData = paymentSpecificData,
      taxType             = TaxTypes.PNGR,
      email               = None
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
      "/tps/testdata/pngr/journey-1-Started.json"
    )

    override lazy val journeyAtPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/pngr/journey-6-AtPciPal.json"
    )

    override lazy val journeyResetByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/pngr/journey-7.a-ResetByPciPal.json"
    )

    override lazy val journeyFinishedByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/pngr/journey-7.b-FinishedByPciPal.json"
    )

    override lazy val journeyBackByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/pngr/journey-7.c-BackByPciPal.json"
    )

    override lazy val journeyReceivedNotificationJson: JourneyJson = JourneyJson(
      "/tps/testdata/pngr/journey-8-ReceivedNotification.json"
    )

  }
}
