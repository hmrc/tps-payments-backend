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
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.Json
import reactivemongo.api.commands.UpdateWriteResult
import repository.TpsRepo
import support.{AuthWireMockResponses, ItSpec, TestConnector, TpsData}

class TpsControllerSpec extends ItSpec {

  val repo = injector.instanceOf[TpsRepo]
  val testConnector = injector.instanceOf[TestConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    val remove = repo.removeAll().futureValue
  }

  "store data when authorised" in {
    AuthWireMockResponses.authorised("PrivilegedApplication", "userId")
    val result = testConnector.store(TpsData.tpsPayments).futureValue
    result shouldBe TpsData.id
  }

  "store data and delete when authorised" in {
    AuthWireMockResponses.authorised("PrivilegedApplication", "userId")
    val result = testConnector.store(TpsData.tpsPayments).futureValue
    result shouldBe TpsData.id
    val resultDelete = testConnector.delete(TpsData.id).futureValue
    resultDelete.status shouldBe Status.OK

  }
  "getId" in {
    AuthWireMockResponses.authorised("PrivilegedApplication", "userId")
    val result = testConnector.getId.futureValue
    result.status shouldBe Status.OK
  }

  "Not authorised should get an exception" in {
    AuthWireMockResponses.notAuthorised
    an[Exception] should be thrownBy testConnector.store(TpsData.tpsPayments).futureValue
  }

  "Insufficient Enrolments should get an exception" in {
    AuthWireMockResponses.failsWith("InsufficientEnrolments")
    an[Exception] should be thrownBy testConnector.store(TpsData.tpsPayments).futureValue
  }

  "Check that TpsData can be found" in {
    AuthWireMockResponses.authorised("PrivilegedApplication", "userId")
    val upserted: UpdateWriteResult = repo.upsert(TpsData.id, TpsData.tpsPayments).futureValue
    upserted.n shouldBe 1
    val result = testConnector.find(TpsData.id).futureValue
    result shouldBe TpsData.tpsPayments
  }

  "Check that TpsData cannot be found" in {
    AuthWireMockResponses.authorised("PrivilegedApplication", "userId")
    val result = testConnector.find(TpsData.id).failed.futureValue
    result.getMessage should include(s"No payments found for id ${TpsData.id.value}")
  }

  "Check that TpsData can be updated with pcipal-sessionId" in {
    AuthWireMockResponses.authorised("PrivilegedApplication", "userId")
    val upserted: UpdateWriteResult = repo.upsert(TpsData.id, TpsData.tpsPayments.copy(pciPalSessionId = None)).futureValue
    upserted.n shouldBe 1
    val result = testConnector.find(TpsData.id).futureValue
    result shouldBe TpsData.tpsPayments.copy(pciPalSessionId = None)
    result.pciPalSessionId shouldBe None
    val updated = testConnector.updateWithSessionId(TpsData.id, TpsData.pciPalSessionId).futureValue
    val result2 = testConnector.find(TpsData.id).futureValue
    result2.pciPalSessionId shouldBe Some(TpsData.pciPalSessionId)

  }

  "update with pci-pal data" in {
    AuthWireMockResponses.authorised("PrivilegedApplication", "userId")
    val upserted: UpdateWriteResult = repo.upsert(TpsData.id, TpsData.tpsPayments).futureValue
    upserted.n shouldBe 1
    val pciPaledUpdated = testConnector.updateTpsPayments(TpsData.chargeRefNotificationPciPalRequest).futureValue
    pciPaledUpdated.status shouldBe Status.OK
    val result = testConnector.find(TpsData.id).futureValue
    result.payments(0).pcipalData match {
      case Some(x) => x shouldBe TpsData.pcipalData
      case None    => throw new RuntimeException("Pcipal data missing")
    }
  }

  "get an exception if pcipalSessionId not found and trying to do an update" in {
    AuthWireMockResponses.authorised("PrivilegedApplication", "userId")
    val upserted: UpdateWriteResult = repo.upsert(TpsData.id, TpsData.tpsPayments).futureValue
    upserted.n shouldBe 1
    val error = testConnector.updateTpsPayments(TpsData.chargeRefNotificationPciPalRequest.copy(PCIPalSessionId = PcipalSessionId("new)"))).failed.futureValue
    error.getMessage should include ("Could not find pcipalSessionId: new")
  }

  "get an exception if paymentItemId not found and trying to do an update" in {
    AuthWireMockResponses.authorised("PrivilegedApplication", "userId")
    val upserted: UpdateWriteResult = repo.upsert(TpsData.id, TpsData.tpsPayments).futureValue
    upserted.n shouldBe 1
    val error = testConnector.updateTpsPayments(TpsData.chargeRefNotificationPciPalRequest.copy(paymentItemId = PaymentItemId("New"))).failed.futureValue
    error.getMessage should include ("Could not find paymentItemId: New")
  }

}
