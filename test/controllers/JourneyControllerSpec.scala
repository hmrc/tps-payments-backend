/*
 * Copyright 2023 HM Revenue & Customs
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

import email.EmailService
import play.api.http.Status
import testsupport.stubs.AuthStub
import testsupport.stubs.AuthStub._
import testsupport.testdata.TestData._
import testsupport.{ItSpec, TestConnector}
import tps.journey.model.{Journey, JourneyId}
import tps.model.{PaymentItemId, TaxTypes}
import tps.pcipalmodel.PcipalSessionId
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse}

class JourneyControllerSpec extends ItSpec with Status {
  private def connector = injector.instanceOf[TestConnector]

  private implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("Bearer xyz")))

  "tpsPayments should transform a payment request from a tps client system into tps data data, store and return the id" in {
    AuthStub.authorised()
    val id = connector.startTpsJourneyMibOrPngr(tpsPaymentRequest).futureValue
    val payment = connector.find(id).futureValue
    payment.payments.headOption.value.paymentSpecificData.getReference shouldBe tpsPaymentRequest.payments.headOption.value.chargeReference
  }

  "store data when authorised" in {
    authorised()
    val result = connector.upsert(journey).futureValue
    result shouldBe ()
  }

  "Not authorised should get an exception" in {
    notAuthenticated()
    an[Exception] should be thrownBy connector.upsert(journey).futureValue
  }

  "Insufficient Enrolments should get an exception" in {
    notAuthorised("InsufficientEnrolments")
    an[Exception] should be thrownBy connector.upsert(journey).futureValue
  }

  "Check that TpsData can be found" in {
    authorised()
    connector.upsert(journey).futureValue
    val result = connector.find(id).futureValue
    result shouldBe journey
  }

  "Check that TpsData cannot be found" in {
    authorised()
    val result = connector.find(id).failed.futureValue
    result.getMessage should include(s"No journey with given id [${id.value}]")
  }

  "update with pci-pal data" in {
    authorised()
    //    connector.upsert(tpsPaymentsWithEncryptedEmail).futureValue
    connector.upsert(tpsPaymentsWithPcipalData).futureValue
    val pciPalUpdated: HttpResponse = connector.updateTpsPayments(chargeRefNotificationPcipalRequest).futureValue
    pciPalUpdated.status shouldBe OK
    val result: Journey = connector.find(id).futureValue
    result.payments.headOption.value.pcipalData.value shouldBe chargeRefNotificationPcipalRequest
  }

  "get an exception if pcipalSessionId not found and trying to do an update" in {
    authorised()
    Option(repo.upsert(journey).futureValue.getUpsertedId).isDefined shouldBe true
    val response = connector.updateTpsPayments(chargeRefNotificationPcipalRequest.copy(PCIPalSessionId = PcipalSessionId("new)"))).futureValue
    response.status shouldBe 400
    response.body should include("Could not find corresponding journey matching pcipalSessionId: [paymentItemId-48c978bb-64b6-4a00-a1f1-51e267d84f91] [PCIPalSessionId:new)] [HoD:B]")
  }

  "get an exception if paymentItemId not found and trying to do an update" in {
    authorised()
    journeyService.upsert(tpsPaymentsWithPcipalData).futureValue
    val nonExistingPaymentItemId: PaymentItemId = PaymentItemId("649965afeb13cd4b9787b054")
    nonExistingPaymentItemId should not be chargeRefNotificationPcipalRequest.paymentItemId withClue "notification comes with different payment item id"
    val response = connector.updateTpsPayments(chargeRefNotificationPcipalRequest.copy(paymentItemId = nonExistingPaymentItemId)).futureValue
    response.status shouldBe 400
    response.body should include("Could not find corresponding journey matching paymentItemId: [649965afeb13cd4b9787b054] [PCIPalSessionId:48c978bb] [HoD:B]")
  }

  "getTaxType should return the correct tax type given the id of a persisted tps payment item" in {
    journeyService.upsert(journey).futureValue
    connector.getPaymentItemTaxType(paymentItemId).futureValue shouldBe TaxTypes.P800
  }

  "getTaxType should return 404 when the tps payment item id is not found" in {
    intercept[Exception] {
      connector.getPaymentItemTaxType(paymentItemId).futureValue
    }.getMessage.contains("404") shouldBe true
  }

  "getTaxType should return 500 when a duplicate id is found" in {
    val tpsIdForDuplicate = JourneyId("session-48c978bb-64b6-4a00-a1f1-51e267d84f92")
    val paymentWithDuplicatePaymentItemId = journey.copy(_id = tpsIdForDuplicate)

    repo.upsert(journey).futureValue
    repo.upsert(paymentWithDuplicatePaymentItemId).futureValue

    intercept[Exception] {
      connector.getPaymentItemTaxType(paymentItemId).futureValue
    }.getMessage.contains("500") shouldBe true
  }

  "should parse TpsPaymentItems for email correctly" in {
    val emailService = app.injector.instanceOf[EmailService]
    val t = tpsPaymentsWithPcipalData.payments.map(emailService.toIndividualPaymentForEmail)
    emailService.stringifyTpsPaymentsItemsForEmail(t) shouldBe tpsItemsForEmail
  }
}
