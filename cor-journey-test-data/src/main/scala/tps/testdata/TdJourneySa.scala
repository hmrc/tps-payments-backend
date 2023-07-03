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

trait TdJourneySa { dependencies: TdBase =>

  object JourneySa extends TdJourneyInStates {

    override lazy val journeyId: JourneyId = dependencies.journeyId
    override lazy val pid: String = dependencies.pid
    override lazy val created: Instant = dependencies.instant
    override lazy val navigation: Navigation = dependencies.navigation
    override lazy val amountString: String = "104.04"
    override lazy val taxReference: String = "1234567895K"

    override lazy val paymentSpecificData: SaSpecificData = SaSpecificData(
      saReference = taxReference
    )

    override lazy val pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId              = dependencies.flowId,
      InitialValues       = List(PcipalInitialValues(
        clientId           = "SAPM",
        pid                = dependencies.pid,
        accountOfficeId    = "S1",
        HODIdentifier      = HeadOfDutyIndicators.K,
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
        taxRegimeDisplay   = "SA",
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

    override lazy val pciPalData: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
      HoD                  = HeadOfDutyIndicators.K,
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

    override lazy val paymentItem: PaymentItem = PaymentItem(
      paymentItemId       = dependencies.paymentItemId,
      amount              = amount,
      headOfDutyIndicator = HeadOfDutyIndicators.K,
      updated             = dependencies.instant,
      customerName        = dependencies.customerName,
      chargeReference     = taxReference,
      pcipalData          = Some(pciPalData),
      paymentSpecificData = paymentSpecificData,
      taxType             = TaxTypes.Sa,
      email               = Some(dependencies.email)
    )
  }
}

