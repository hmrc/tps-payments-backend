/*
 * Copyright 2020 HM Revenue & Customs
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

import model.PaymentItemId
import model.pcipal.PcipalSessionId
import play.api.http.Status
import support.TpsData.{chargeRefNotificationPciPalRequest, id, tpsPayments}
import support.{AuthWireStub, ItSpec, TestConnector, TpsData}

class TpsControllerSpec extends ItSpec with Status {
  private lazy val connector = injector.instanceOf[TestConnector]

  "store data when authorised" in {
    AuthWireStub.authorised()
    val result = connector.store(tpsPayments).futureValue
    result shouldBe id
  }

  "store data and delete when authorised" in {
    AuthWireStub.authorised()
    val result = connector.store(tpsPayments).futureValue
    result shouldBe id
    val resultDelete = connector.delete(id).futureValue
    resultDelete.status shouldBe OK

  }
  "getId" in {
    AuthWireStub.authorised()
    val result = connector.getId.futureValue
    result.status shouldBe OK
  }

  "Not authorised should get an exception" in {
    AuthWireStub.notAuthorised
    an[Exception] should be thrownBy connector.store(tpsPayments).futureValue
  }

  "Insufficient Enrolments should get an exception" in {
    AuthWireStub.failsWith("InsufficientEnrolments")
    an[Exception] should be thrownBy connector.store(tpsPayments).futureValue
  }

  "Check that TpsData can be found" in {
    AuthWireStub.authorised()
    repo.upsert(id, tpsPayments).futureValue.n shouldBe 1
    val result = connector.find(id).futureValue
    result shouldBe tpsPayments
  }

  "Check that TpsData cannot be found" in {
    AuthWireStub.authorised()
    val result = connector.find(id).failed.futureValue
    result.getMessage should include(s"No payments found for id ${id.value}")
  }

  "Check that TpsData can be updated with pcipal-sessionId" in {
    AuthWireStub.authorised()
    repo.upsert(id, tpsPayments.copy(pciPalSessionId = None)).futureValue.n shouldBe 1
    val result = connector.find(id).futureValue
    result shouldBe tpsPayments.copy(pciPalSessionId = None)
    result.pciPalSessionId shouldBe None
    connector.updateWithSessionId(id, TpsData.pciPalSessionId).futureValue
    val result2 = connector.find(id).futureValue
    result2.pciPalSessionId shouldBe Some(TpsData.pciPalSessionId)

  }

  "update with pci-pal data" in {
    AuthWireStub.authorised()
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
    AuthWireStub.authorised()
    repo.upsert(id, tpsPayments).futureValue.n shouldBe 1
    val error = connector.updateTpsPayments(chargeRefNotificationPciPalRequest.copy(PCIPalSessionId = PcipalSessionId("new)"))).failed.futureValue
    error.getMessage should include ("Could not find pcipalSessionId: new")
  }

  "get an exception if paymentItemId not found and trying to do an update" in {
    AuthWireStub.authorised()
    repo.upsert(id, tpsPayments).futureValue.n shouldBe 1
    val error = connector.updateTpsPayments(chargeRefNotificationPciPalRequest.copy(paymentItemId = PaymentItemId("New"))).failed.futureValue
    error.getMessage should include ("Could not find paymentItemId: New")
  }

}
