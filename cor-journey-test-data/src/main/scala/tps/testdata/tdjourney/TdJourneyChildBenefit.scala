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

package tps.testdata.tdjourney

import tps.journey.model.JourneyId
import tps.model._
import tps.pcipalmodel._
import tps.testdata.TdBase
import tps.testdata.util.JourneyJson

import java.time.Instant

trait TdJourneyChildBenefit { dependencies: TdBase =>

  object TdJourneyChildBenefit extends TdJourneyInStates {

    override lazy val journeyId: JourneyId = dependencies.journeyId
    override lazy val pid: String = dependencies.pid
    override lazy val created: Instant = dependencies.instant
    override lazy val navigation: Navigation = dependencies.navigation
    override lazy val amountEditedString: String = "103.03"
    override lazy val taxReference: String = "YA123456789123"
    override final val selectedTaxType: TpsNativeTaxType = TaxTypes.ChildBenefitsRepayments

    override lazy val pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId              = dependencies.flowId,
      InitialValues       = List(PcipalInitialValues(
        clientId           = "CBCE",
        pid                = dependencies.pid,
        accountOfficeId    = "S1",
        HODIdentifier      = HeadOfDutyIndicators.B,
        UTRReference       = taxReference,
        name1              = dependencies.customerName.value,
        amount             = amountEditedString,
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
        taxRegimeDisplay   = "Repay Child Benefit overpayments",
        reference          = dependencies.pciPalReferenceNumber,
        increment          = "1"
      )),
      UTRBlacklistFlag    = "N",
      postcodeFlag        = "Y",
      taxRegime           = "gen",
      TotalTaxAmountToPay = amountEditedString,
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
      Amount               = amountEdited,
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

    override lazy val paymentItemEntered: PaymentItem = PaymentItem(
      paymentItemId       = dependencies.paymentItemId,
      amount              = amountEntered,
      headOfDutyIndicator = HeadOfDutyIndicators.B,
      updated             = instant,
      customerName        = dependencies.customerName,
      chargeReference     = taxReference,
      pcipalData          = None,
      paymentSpecificData = ChildBenefitSpecificData(
        childBenefitYReference = taxReference
      ),
      taxType             = TaxTypes.ChildBenefitsRepayments,
      email               = Some(dependencies.email)
    )

    override lazy val paymentItemAfterReceivedNotification: PaymentItem = paymentItemEdited.copy(pcipalData = Some(pcipalData))

    override lazy val journeyStartedJson = JourneyJson(
      "/tps/testdata/childbenefit/journey-1-Started.json"
    )

    override lazy val journeyInEnterPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/childbenefit/journey-2-InEnterPaymentJson.json"
    )

    override lazy val journeyWithEnteredPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/childbenefit/journey-3-WithEnteredPaymentInTheBasket.json"
    )

    override def journeyInEditPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/childbenefit/journey-4-InEditPayment.json"
    )

    override def journeyWithEditedPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/childbenefit/journey-5-WithEditedPayment.json"
    )

    override lazy val journeyAtPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/childbenefit/journey-6-AtPciPal.json"
    )

    override lazy val journeyResetByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/childbenefit/journey-7.a-ResetByPciPal.json"
    )

    override lazy val journeyFinishedByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/childbenefit/journey-7.b-FinishedByPciPal.json"
    )

    override lazy val journeyBackByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/childbenefit/journey-7.c-BackByPciPal.json"
    )

    override lazy val journeyReceivedNotificationJson: JourneyJson = JourneyJson(
      "/tps/testdata/childbenefit/journey-8-ReceivedNotification.json"
    )

  }
}
