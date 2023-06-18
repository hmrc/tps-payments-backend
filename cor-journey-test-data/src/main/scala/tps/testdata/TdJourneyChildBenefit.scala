package tps.testdata

import tps.model._
import tps.pcipalmodel._

import java.time.Instant

trait TdJourneyChildBenefit { dependencies: TdBase =>


  def tdJourneyInStatesChildBenefit = new TdJourneyInStates {

    override def journeyId: JourneyId = dependencies.journeyId
    override def pid: String = dependencies.pid
    override def created: Instant = dependencies.instant
    override def navigation: Navigation = dependencies.navigation
    override def amountString: String = "103.03"
    override def taxReference: String = "YA123456789123"

    override def paymentSpecificData = ChildBenefitSpecificData(
      childBenefitYReference = taxReference
    )

    override def pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId = dependencies.flowId,
      InitialValues = List(PcipalInitialValues(
        clientId = "CBCE",
        pid = dependencies.pid,
        accountOfficeId = "S1",
        HODIdentifier = HeadOfDutyIndicators.B,
        UTRReference = taxReference,
        name1 = dependencies.customerName,
        amount = amountString,
        taxAmount = None,
        nicAmount = None,
        lnpClass2 = None,
        nirRate = None,
        startDate = None,
        endDate = None,
        vatPeriodReference = None,
        vatRemittanceType = None,
        paymentItemId = dependencies.paymentItemId,
        chargeReference = taxReference,
        taxRegimeDisplay = "Repay Child Benefit overpayments",
        reference = dependencies.pciPalReferenceNumber,
        increment = "1"
      )),
      UTRBlacklistFlag = "N",
      postcodeFlag = "Y",
      taxRegime = "gbl",
      TotalTaxAmountToPay = amountString,
      callbackUrl = navigation.callback,
      backUrl = navigation.back,
      resetUrl = navigation.reset,
      finishUrl = navigation.finish
    )

    override def pcipalSessionLaunchResponse: PcipalSessionLaunchResponse = PcipalSessionLaunchResponse(
      Id = dependencies.pciPalSessionId,
      LinkId = dependencies.linkId
    )

    override def pciPalData: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
      HoD = HeadOfDutyIndicators.B,
      TaxReference = taxReference,
      Amount = amount,
      Commission = 0,
      CardType = dependencies.cardType,
      Status = StatusTypes.validated,
      PCIPalSessionId = dependencies.pciPalSessionId,
      TransactionReference = taxReference,
      paymentItemId = dependencies.paymentItemId,
      ChargeReference = taxReference,
      ReferenceNumber = dependencies.pciPalReferenceNumber,
      CardLast4 = dependencies.cardLast4Digits
    )

    override def paymentItem: PaymentItem = PaymentItem(
      paymentItemId = Some(dependencies.paymentItemId),
      amount = amount,
      headOfDutyIndicator = HeadOfDutyIndicators.B,
      updated = instant,
      customerName = dependencies.customerName,
      chargeReference = taxReference,
      pcipalData = Some(pciPalData),
      paymentSpecificData = paymentSpecificData,
      taxType = TaxTypes.ChildBenefitsRepayments,
      email = Some(dependencies.email)
    )
  }
}
