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

import play.api.http.Status
import testsupport.stubs.AuthStub
import testsupport.testdata.TestData._
import testsupport.{ItSpec, TestConnector}
import tps.journey.model.JourneyId
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}

class PaymentItemProcessorControllerSpec extends ItSpec with Status:

  private given HeaderCarrier = HeaderCarrier(Some(Authorization("Bearer xyz")))

  private lazy val connector = injector.instanceOf[TestConnector]

  "getModsAmendmentRef should return the amendment reference in the ModsPaymentCallBackRequest when there is one" in {
    AuthStub.authorised()
    connector.upsert(modsTpsPaymentsWithAnAmendmentReference).futureValue
    connector
      .getModsPaymentItemAmendmentReference(paymentItemId)
      .futureValue shouldBe modsPaymentCallBackRequestWithAmendmentRef
  }

  "getModsAmendmentRef should return None for amendment reference in the ModsPaymentCallBackRequest when isn't one" in {
    AuthStub.authorised()
    connector.upsert(modsTpsPaymentsNoAmendmentReference).futureValue
    connector
      .getModsPaymentItemAmendmentReference(paymentItemId)
      .futureValue shouldBe modsPaymentCallBackRequestWithoutAmendmentRef
  }

  "getModsAmendmentRef should return 500 when a duplicate id is found" in {
    val tpsIdForDuplicate                 = JourneyId("session-48c978bb-64b6-4a00-a1f1-51e267d84f92")
    val paymentWithDuplicatePaymentItemId = journey.copy(_id = tpsIdForDuplicate)

    repo.upsert(journey).futureValue
    repo.upsert(paymentWithDuplicatePaymentItemId).futureValue

    intercept[Exception] {
      connector.getModsPaymentItemAmendmentReference(paymentItemId).futureValue
    }.getMessage.contains("500") shouldBe true
  }

  "propagate error from findModsPaymentsByReference if payment specific data is not MibSpecificData" in {
    journeyService.upsert(tpsPaymentsWithEmptyEmail).futureValue
    intercept[Exception] {
      connector.getModsPaymentItemAmendmentReference(paymentItemId).futureValue
    }.getMessage should include(
      s"No payment items with this id [ ${paymentItemId.value} ], it's not mods, why is it being looked up?"
    )
  }

  "propagate error from findModsPaymentsByReference if no payment item is found" in {
    intercept[Exception] {
      connector.getModsPaymentItemAmendmentReference(paymentItemId).futureValue
    }.getMessage should include(s"No payment specific data for id [ ${paymentItemId.value} ]")
  }
