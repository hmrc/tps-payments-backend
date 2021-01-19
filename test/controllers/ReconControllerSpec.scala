package controllers

import model.TaxType.P800
import model.pcipal.PcipalSessionId
import model.{PaymentItemId, TpsId}
import play.api.http.Status
import recon.FindRPaymentSpecificDataRequest
import support.AuthStub._
import support.TpsData._
import support.{ItSpec, TestConnector}
import uk.gov.hmrc.http.HeaderCarrier

class ReconControllerSpec extends ItSpec with Status {

  private implicit val emptyHC: HeaderCarrier = HeaderCarrier()

  private lazy val connector = injector.instanceOf[TestConnector]

  "Recon connector should surface mods info correctly when they exist" in {
    repo.upsert(id, modsTpsPayments).futureValue
    connector.findModsPayments(FindRPaymentSpecificDataRequest(modsLookupChargeRefs)).futureValue.body shouldBe modsReconLookupJson.toString()
  }

}
