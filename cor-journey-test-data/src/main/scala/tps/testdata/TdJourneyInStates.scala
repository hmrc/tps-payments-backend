package tps.testdata

import tps.model.{Journey, JourneyId, Navigation, PaymentItem, PaymentItemId, PaymentSpecificData, Reference, TaxType}
import tps.pcipalmodel.{ChargeRefNotificationPcipalRequest, PcipalSessionLaunchRequest, PcipalSessionLaunchResponse}

import java.time.Instant

trait TdJourneyInStates {
  def journeyId: JourneyId
  def pid: String
  def created: Instant
  def navigation: Navigation

  def amountString: String
  final def amount: BigDecimal = BigDecimal(amountString)

  //TODO: provide a strong type for that, use it in Journey, etc. Make sure you don't break existing json formats
  def taxReference: String

  def pcipalSessionLaunchRequest: PcipalSessionLaunchRequest
  def pcipalSessionLaunchResponse: PcipalSessionLaunchResponse
  def pciPalData: ChargeRefNotificationPcipalRequest
  def paymentItem: PaymentItem
  def paymentSpecificData: PaymentSpecificData

  //TODO: this is in one particular (final) state
  def journey = Journey(
    _id = journeyId,
    pid = pid,
    created = created,
    payments = List(paymentItem),
    navigation = Some(navigation),
    pcipalSessionLaunchRequest = Some(pcipalSessionLaunchRequest),
    pcipalSessionLaunchResponse = Some(pcipalSessionLaunchResponse)
  )

}
