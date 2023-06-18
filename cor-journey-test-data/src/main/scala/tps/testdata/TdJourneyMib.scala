package tps.testdata

import tps.model.{HeadOfDutyIndicators, JourneyId, MibSpecificData, Navigation, PaymentItem, PaymentItemId, PngrSpecificData, TaxType, TaxTypes}
import tps.pcipalmodel.{ChargeRefNotificationPcipalRequest, PcipalInitialValues, PcipalSessionLaunchRequest, PcipalSessionLaunchResponse, StatusType, StatusTypes}
import tps.startjourneymodel.{SjPaymentItem, StartJourneyRequestMibOrPngr}

import java.time.Instant

trait TdJourneyMib { dependencies: TdBase =>

  def tdJourneyInStatesMib = new TdJourneyInStates {

    def startJourneyRequest: StartJourneyRequestMibOrPngr = StartJourneyRequestMibOrPngr(
      pid = dependencies.pid,
      payments = Seq[SjPaymentItem](
        SjPaymentItem(
          chargeReference = taxReference,
          customerName = dependencies.customerName,
          amount = amount,
          taxRegimeDisplay = "MODS", //https://github.com/hmrc/merchandise-in-baggage-frontend/blob/96027c5b4cdbc4f957c06b0c7e295861962f2432/app/uk/gov/hmrc/merchandiseinbaggage/model/api/tpspayments/TpsPaymentsItem.scala#L25
          taxType = TaxTypes.MIB,
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
    override def amountString: String = "102.02"
    override def taxReference: String = "XJPR5573376231"

    override def paymentSpecificData: MibSpecificData = MibSpecificData(
      chargeReference = taxReference,
      vat = BigDecimal("0.07"),
      customs = BigDecimal("0.05"),
      amendmentReference = Some(123)
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
      CardLast4 =dependencies.cardLast4Digits
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
      taxType = TaxTypes.PNGR,
      email = Some(dependencies.email)
    )

  }


}
