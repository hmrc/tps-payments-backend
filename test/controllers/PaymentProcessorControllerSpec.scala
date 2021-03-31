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

import model.TpsId
import play.api.http.Status
import support.TpsData._
import support.{ItSpec, TestConnector}
import uk.gov.hmrc.http.HeaderCarrier

class PaymentProcessorControllerSpec extends ItSpec with Status {

  private implicit val emptyHC: HeaderCarrier = HeaderCarrier()

  private lazy val connector = injector.instanceOf[TestConnector]

  "getModsAmendmentRef should return the amendment reference in the ModsPaymentCallBackRequest when there is one" in {
    repo.upsert(id, modsTpsPaymentsWithAnAmendmentReference).futureValue.n shouldBe 1
    connector.getModsPaymentItemAmendmentReference(paymentItemId).futureValue shouldBe modsPaymentCallBackRequestWithAmendmentRef
  }

  "getModsAmendmentRef should return None for amendment reference in the ModsPaymentCallBackRequest when isn't one" in {
    repo.upsert(id, modsTpsPaymentsNoAmendmentReference).futureValue.n shouldBe 1
    connector.getModsPaymentItemAmendmentReference(paymentItemId).futureValue shouldBe modsPaymentCallBackRequestWithoutAmendmentRef
  }

  "getModsAmendmentRef should return 500 when a duplicate id is found" in {
    val tpsIdForDuplicate = TpsId("session-48c978bb-64b6-4a00-a1f1-51e267d84f92")
    val paymentWithDuplicatePaymentItemId = tpsPayments.copy(_id = tpsIdForDuplicate)

    repo.upsert(id, tpsPayments).futureValue
    repo.upsert(tpsIdForDuplicate, paymentWithDuplicatePaymentItemId).futureValue

    intercept[Exception] {
      connector.getModsPaymentItemAmendmentReference(paymentItemId).futureValue
    }.getMessage.contains("500") shouldBe true
  }
}
