/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import model.TaxType.P800
import model.pcipal.PcipalSessionId
import model.{PaymentItemId, TpsId}
import play.api.http.Status
import support.AuthStub._
import support.TpsData._
import support.{ItSpec, TestConnector}
import uk.gov.hmrc.http.HeaderCarrier

class TpsControllerSpec extends ItSpec with Status {
  private implicit val emptyHC: HeaderCarrier = HeaderCarrier()

  private lazy val connector = injector.instanceOf[TestConnector]

  "tpsPayments should transform a payment request from a tps client system into tps data data, store and return the id" in {
    givenTheUserIsAuthenticatedAndAuthorised()

    val id = connector.tpsPayments(tpsPaymentRequest).futureValue
    val payment = connector.find(id).futureValue

    payment.payments.head.paymentSpecificData.getReference shouldBe tpsPaymentRequest.payments.head.chargeReference
  }

  "store data when authorised" in {
    givenTheUserIsAuthenticatedAndAuthorised()
    val result = connector.store(tpsPayments).futureValue
    result shouldBe id
  }

  "store data and delete when authorised" in {
    givenTheUserIsAuthenticatedAndAuthorised()
    val result = connector.store(tpsPayments).futureValue
    result shouldBe id
    val resultDelete = connector.delete(id).futureValue
    resultDelete.status shouldBe OK

  }
  "getId" in {
    givenTheUserIsAuthenticatedAndAuthorised()
    val result = connector.getId.futureValue
    result.status shouldBe OK
  }

  "Not authorised should get an exception" in {
    givenTheUserIsNotAuthenticated()
    an[Exception] should be thrownBy connector.store(tpsPayments).futureValue
  }

  "Insufficient Enrolments should get an exception" in {
    givenTheUserIsNotAuthorised("InsufficientEnrolments")
    an[Exception] should be thrownBy connector.store(tpsPayments).futureValue
  }

  "Check that TpsData can be found" in {
    givenTheUserIsAuthenticatedAndAuthorised()
    repo.upsert(id, tpsPayments).futureValue.n shouldBe 1
    val result = connector.find(id).futureValue
    result shouldBe tpsPayments
  }

  "Check that TpsData cannot be found" in {
    givenTheUserIsAuthenticatedAndAuthorised()
    val result = connector.find(id).failed.futureValue
    result.getMessage should include(s"No payments found for id ${id.value}")
  }

  "Check that TpsData can be updated with pcipal-sessionId" in {
    givenTheUserIsAuthenticatedAndAuthorised()
    repo.upsert(id, tpsPayments.copy(pciPalSessionId = None)).futureValue.n shouldBe 1
    val result = connector.find(id).futureValue
    result shouldBe tpsPayments.copy(pciPalSessionId = None)
    result.pciPalSessionId shouldBe None
    connector.updateWithSessionId(id, pciPalSessionId).futureValue
    val result2 = connector.find(id).futureValue
    result2.pciPalSessionId shouldBe Some(pciPalSessionId)

  }

  "update with pci-pal data" in {
    givenTheUserIsAuthenticatedAndAuthorised()
    repo.upsert(id, tpsPayments).futureValue.n shouldBe 1
    val pciPaledUpdated = connector.updateTpsPayments(chargeRefNotificationPciPalRequest).futureValue
    pciPaledUpdated.status shouldBe OK
    val result = connector.find(id).futureValue
    result.payments.head.pcipalData match {
      case Some(x) => x shouldBe chargeRefNotificationPciPalRequest
      case None    => throw new RuntimeException("Pcipal data missing")
    }
  }

  "get an exception if pcipalSessionId not found and trying to do an update" in {
    givenTheUserIsAuthenticatedAndAuthorised()
    repo.upsert(id, tpsPayments).futureValue.n shouldBe 1
    val response = connector.updateTpsPayments(chargeRefNotificationPciPalRequest.copy(PCIPalSessionId = PcipalSessionId("new)"))).futureValue
    response.status shouldBe 400
    response.body should include("Could not find pcipalSessionId: new")
  }

  "get an exception if paymentItemId not found and trying to do an update" in {
    givenTheUserIsAuthenticatedAndAuthorised()
    repo.upsert(id, tpsPayments).futureValue.n shouldBe 1
    val response = connector.updateTpsPayments(chargeRefNotificationPciPalRequest.copy(paymentItemId = PaymentItemId("New"))).futureValue
    response.status shouldBe 400
    response.body should include("Could not find paymentItemId: New")
  }

  "getTaxType should return the correct tax type given the id of a persisted tps payment item" in {
    repo.upsert(id, tpsPayments).futureValue.n shouldBe 1
    connector.getPaymentItemTaxType(paymentItemId).futureValue shouldBe P800
  }

  "getTaxType should return 404 when the tps payment item id is not found" in {
    intercept[Exception] {
      connector.getPaymentItemTaxType(paymentItemId).futureValue
    }.getMessage.contains("404") shouldBe true
  }

  "getTaxType should return 500 when a duplicate id is found" in {
    val tpsIdForDuplicate = TpsId("session-48c978bb-64b6-4a00-a1f1-51e267d84f92")
    val paymentWithDuplicatePaymentItemId = tpsPayments.copy(_id = tpsIdForDuplicate)

    repo.upsert(id, tpsPayments).futureValue
    repo.upsert(tpsIdForDuplicate, paymentWithDuplicatePaymentItemId).futureValue

    intercept[Exception] {
      connector.getPaymentItemTaxType(paymentItemId).futureValue
    }.getMessage.contains("500") shouldBe true
  }
}
