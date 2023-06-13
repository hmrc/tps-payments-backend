package tps.testdata

import tps.model.TaxTypes
import tps.startjourneymodel.{SjPaymentItem, StartJourneyRequestMibOrPngr}

trait StartJourneyRequest { dependencies: TdBase with TdNavigation with TdMib with TdPngr=>

  def startJourneyRequestMib: StartJourneyRequestMibOrPngr = StartJourneyRequestMibOrPngr(
    pid = dependencies.pid,
    payments = Seq[SjPaymentItem](
      SjPaymentItem(
        chargeReference = dependencies.chargeReference,
        customerName = dependencies.customerName,
        amount = dependencies.amount,
        taxRegimeDisplay = "MODS",
        taxType = TaxTypes.MIB,
        paymentSpecificData = dependencies.mibSpecificData,
        email = Some(dependencies.email)
      )
    ),
    navigation = dependencies.navigation
  )

  def startJourneyRequestPngr: StartJourneyRequestMibOrPngr = StartJourneyRequestMibOrPngr(
    pid = "pid",
    payments = Seq[SjPaymentItem](
      SjPaymentItem(
        chargeReference = dependencies.chargeReference,
        customerName = dependencies.customerName,
        amount = dependencies.amount,
        taxRegimeDisplay = "PNGR",
        taxType = TaxTypes.PNGR,
        paymentSpecificData = dependencies.pngrSpecificData,
        email = Some(dependencies.email)
      )
    ),
    navigation = dependencies.navigation
  )

}
