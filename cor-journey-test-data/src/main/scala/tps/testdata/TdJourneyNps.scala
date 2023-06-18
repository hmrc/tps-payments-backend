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

trait TdJourneyNps { dependencies: TdBase =>

  object JourneyNps extends TdJourneyInStates {

    override def journeyId: JourneyId = dependencies.journeyId
    override def pid: String = dependencies.pid
    override def created: Instant = dependencies.instant
    override def navigation: Navigation = dependencies.navigation
    override def amountString: String = "110.10"
    override def taxReference: String = "AA000000JM"

    override def paymentSpecificData: NpsSpecificData = NpsSpecificData(
      npsReference    = taxReference,
      periodStartDate = "020122",
      periodEndDate   = "080122",
      npsType         = "Class 2 National Insurance",
      rate            = 1
    )

    override def pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId              = dependencies.flowId,
      InitialValues       = List(PcipalInitialValues(
        clientId           = "NPPL",
        pid                = dependencies.pid,
        accountOfficeId    = "G1",
        HODIdentifier      = HeadOfDutyIndicators.J,
        UTRReference       = taxReference,
        name1              = dependencies.customerName,
        amount             = amountString,
        taxAmount          = None,
        nicAmount          = None,
        lnpClass2          = Some("61"),
        nirRate            = Some("1.0"),
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

    override def pcipalSessionLaunchResponse: PcipalSessionLaunchResponse = PcipalSessionLaunchResponse(
      Id     = dependencies.pciPalSessionId,
      LinkId = dependencies.linkId
    )

    override def pciPalData: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
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

    override def paymentItem: PaymentItem = PaymentItem(
      paymentItemId       = Some(dependencies.paymentItemId),
      amount              = amount,
      headOfDutyIndicator = HeadOfDutyIndicators.J,
      updated             = dependencies.instant,
      customerName        = dependencies.customerName,
      chargeReference     = taxReference,
      pcipalData          = Some(pciPalData),
      paymentSpecificData = paymentSpecificData,
      taxType             = TaxTypes.Nps,
      email               = Some(dependencies.email)
    )

  }

}
