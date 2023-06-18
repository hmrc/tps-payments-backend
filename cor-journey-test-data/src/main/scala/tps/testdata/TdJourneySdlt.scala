package tps.testdata

import tps.model._
import tps.pcipalmodel._
import tps.startjourneymodel.{SjPaymentItem, StartJourneyRequestMibOrPngr}

import java.time.Instant

trait TdJourneySdlt { dependencies: TdBase=>

  def tdJourneyInStatesSdlt = new TdJourneyInStates {

    override def journeyId: JourneyId = dependencies.journeyId
    override def pid: String = dependencies.pid
    override def created: Instant = dependencies.instant
    override def navigation: Navigation = dependencies.navigation
    override def amountString: String = "105.05"
    override def taxReference: String = "123456789MA"

    override def paymentSpecificData: SdltSpecificData = SdltSpecificData(
      sdltReference = taxReference
    )

    override def pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId = dependencies.flowId,
      InitialValues = List(PcipalInitialValues(
        clientId = "SDPL",
        pid = dependencies.pid,
        accountOfficeId = "S1",
        HODIdentifier = HeadOfDutyIndicators.M,
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
        taxRegimeDisplay = "SDLT",
        reference = dependencies.pciPalReferenceNumber,
        increment = "1"
      )),
      UTRBlacklistFlag = "N",
      postcodeFlag = "Y",
      taxRegime = "gen",
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
      HoD = HeadOfDutyIndicators.M,
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
      headOfDutyIndicator = HeadOfDutyIndicators.M,
      updated = dependencies.instant,
      customerName = dependencies.customerName,
      chargeReference = taxReference,
      pcipalData = Some(pciPalData),
      paymentSpecificData = paymentSpecificData,
      taxType = TaxTypes.Sdlt,
      email = Some(dependencies.email)
    )

  }

}
