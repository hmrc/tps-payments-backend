package tps.testdata

import tps.model.{HeadOfDutyIndicators, JourneyId, Navigation, PaymentItem, PaymentItemId, PngrSpecificData, TaxTypes}
import tps.pcipalmodel.{ChargeRefNotificationPcipalRequest, PcipalInitialValues, PcipalSessionLaunchRequest, PcipalSessionLaunchResponse, StatusType, StatusTypes}
import tps.startjourneymodel.{SjPaymentItem, StartJourneyRequestMibOrPngr}

import java.time.Instant

trait TdJourneyPngr { dependencies: TdBase=>

  def tdJourneyInStatesPngr = new TdJourneyInStates {

    def startJourneyRequest: StartJourneyRequestMibOrPngr = StartJourneyRequestMibOrPngr(
      pid = dependencies.pid,
      payments = Seq[SjPaymentItem](
        SjPaymentItem(
          chargeReference = taxReference,
          customerName = dependencies.customerName,
          amount = amount,
          taxRegimeDisplay = "PNGR", //yes, "PNGR" -> https://github.com/hmrc/bc-passengers-stride-frontend/blob/c4bf6dc0c52c02dc46b05f8990e679992830a03b/app/services/PayApiService.scala#L77
          taxType = TaxTypes.PNGR,
          paymentSpecificData = paymentSpecificData,
          email = Some(dependencies.email)
        )
      ),
      navigation = dependencies.navigation
    )

    override def journeyId: JourneyId = dependencies.journeyId
    override def pid: String = dependencies.pid
    override def created: Instant = dependencies.instant
    override def navigation: Navigation = dependencies.navigation
    override def amountString: String = "101.01"
    override def taxReference: String = "XSPR5814332193"

    override def paymentSpecificData: PngrSpecificData = PngrSpecificData(
      chargeReference = taxReference,
      vat = BigDecimal("297.25"),
      customs = BigDecimal("150.00"),
      excise = BigDecimal("136.27")
    )

    override def pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId = dependencies.flowId,
      InitialValues = List(PcipalInitialValues(
        clientId = "PSML",
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
        taxRegimeDisplay = "PNGR",
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
      updated = dependencies.instant,
      customerName = dependencies.customerName,
      chargeReference = taxReference,
      pcipalData = Some(pciPalData),
      paymentSpecificData = paymentSpecificData,
      taxType = TaxTypes.ChildBenefitsRepayments,
      email = Some(dependencies.email)
    )

  }

}
