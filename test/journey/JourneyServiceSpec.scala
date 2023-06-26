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

package journey

import testsupport.ItSpec
import testsupport.testdata.TestData._
import tps.journey.model.JourneyId
import tps.pcipalmodel.PcipalSessionId

class JourneyServiceSpec extends ItSpec {

  "findByPcipalSessionId should throw error when more than one payment found" in {
    val journeyService: JourneyService = app.injector.instanceOf[JourneyService]

    Option(repo.upsert(tpsPaymentsWithPcipalData).futureValue.getUpsertedId).isDefined shouldBe true
    Option(repo.upsert(tpsPaymentsWithPcipalData.copy(_id = JourneyId("session-48c978bb-64b6-4a00-a1f1-51e267some-new-one"))).futureValue.getUpsertedId).isDefined shouldBe true
    intercept[Exception] {
      journeyService.findByPcipalSessionId(PcipalSessionId("48c978bb")).futureValue
    }.getMessage should include("Found 2 journeys with given pcipalSessionId [48c978bb]")
  }

  "findPaymentItem should optionally find the matching payment item" in {
    val journeyService: JourneyService = app.injector.instanceOf[JourneyService]

    journeyService.findPaymentItem(paymentItemId).futureValue shouldBe None
    journeyService.upsert(tpsPayments).futureValue
    journeyService.findPaymentItem(paymentItemId).futureValue shouldBe Some(tpsPayments.payments.headOption.value)
  }

}
