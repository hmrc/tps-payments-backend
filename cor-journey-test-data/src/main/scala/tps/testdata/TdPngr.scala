package tps.testdata

import tps.model.PngrSpecificData

trait TdPngr {

  def pngrChargeReference: String = "XSPR5814332193"
  def pngrVat: BigDecimal = BigDecimal("297.25")
  def pngrCustoms: BigDecimal = BigDecimal("150.00")
  def pngrExcise: BigDecimal = BigDecimal("136.27")

  def pngrSpecificData: PngrSpecificData = PngrSpecificData(
    chargeReference = pngrChargeReference,
    vat = pngrVat,
    customs = pngrCustoms,
    excise = pngrExcise
  )



}
