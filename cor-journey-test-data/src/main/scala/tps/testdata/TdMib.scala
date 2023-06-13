package tps.testdata

import tps.model.MibSpecificData

trait TdMib {

  def mibChargeReference: String = "XJPR5573376231"
  def mibAmendmentReference: Int = 123
  def mibVat: BigDecimal = BigDecimal("0.07")
  def mibCustoms: BigDecimal = BigDecimal("0.05")

  def mibSpecificData: MibSpecificData = MibSpecificData(
    chargeReference = mibChargeReference,
    vat = mibVat,
    customs = mibCustoms,
    amendmentReference = Some(mibAmendmentReference)
  )
}
