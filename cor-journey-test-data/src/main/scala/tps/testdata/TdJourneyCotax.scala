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

import tps.model._
import tps.pcipalmodel._

import java.time.Instant

trait TdJourneyCotax { dependencies: TdBase =>

  object JourneyCotax extends TdJourneyInStates {

    override def journeyId: JourneyId = dependencies.journeyId
    override def pid: String = dependencies.pid
    override def created: Instant = dependencies.instant
    override def navigation: Navigation = dependencies.navigation
    override def amountString: String = "107.07"
    override def taxReference: String = "1097172564A00104A"

    override def paymentSpecificData: CotaxSpecificData = CotaxSpecificData(
      cotaxReference = taxReference
    )

    override def pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId              = dependencies.flowId,
      InitialValues       = List(PcipalInitialValues(
        clientId           = "COPL",
        pid                = dependencies.pid,
        accountOfficeId    = "S1",
        HODIdentifier      = HeadOfDutyIndicators.A,
        UTRReference       = taxReference,
        name1              = dependencies.customerName,
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
        taxRegimeDisplay   = "COTAX",
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

    override def pcipalSessionLaunchResponse: PcipalSessionLaunchResponse = PcipalSessionLaunchResponse(
      Id     = dependencies.pciPalSessionId,
      LinkId = dependencies.linkId
    )

    override def pciPalData: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
      HoD                  = HeadOfDutyIndicators.A,
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

    override def paymentItem: PaymentItem = PaymentItem(
      paymentItemId       = Some(dependencies.paymentItemId),
      amount              = amount,
      headOfDutyIndicator = HeadOfDutyIndicators.A,
      updated             = dependencies.instant,
      customerName        = dependencies.customerName,
      chargeReference     = taxReference,
      pcipalData          = Some(pciPalData),
      paymentSpecificData = paymentSpecificData,
      taxType             = TaxTypes.Cotax,
      email               = Some(dependencies.email)
    )
  }
}

