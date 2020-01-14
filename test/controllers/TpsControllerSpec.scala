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

import play.api.http.Status
import repository.TpsRepo
import support.{AuthWireMockResponses, ItSpec, TestConnector, TpsData}

class TpsControllerSpec extends ItSpec {

  val repo = injector.instanceOf[TpsRepo]
  val testConnector = injector.instanceOf[TestConnector]

  override def beforeEach(): Unit = {
    val remove = repo.removeAll().futureValue
  }

  "store data when authorised" in {
    AuthWireMockResponses.authorised("PrivilegedApplication", "userId")
    val result = testConnector.store(TpsData.tpsPayments).futureValue
    result.status shouldBe Status.OK
    result.body shouldBe "updated 1 records"
  }

  "Not authorised should get an exception" in {
    AuthWireMockResponses.notAuthorised
    an[Exception] should be thrownBy testConnector.store(TpsData.tpsPayments).futureValue
  }

  "Insufficient Enrolments should get an exception" in {
    AuthWireMockResponses.failsWith("InsufficientEnrolments")
    an[Exception] should be thrownBy testConnector.store(TpsData.tpsPayments).futureValue
  }

}
