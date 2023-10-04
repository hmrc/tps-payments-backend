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

package tps.journey

import journeysupport.{TestJourneyIdGenerator, TestPaymentItemIdGenerator}
import play.api.mvc.Request
import testsupport.ItSpec
import testsupport.stubs.AuthStub
import tps.journey.model.{Journey, JourneyId, StartJourneyResponse}
import tps.model.PaymentItemId
import tps.startjourneymodel.StartJourneyRequestMib
import tps.testdata.TdAll
import uk.gov.hmrc.http.UpstreamErrorResponse

@SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
class StartJourneyControllerMibSpec extends ItSpec {

  private def journeyIdGenerator: TestJourneyIdGenerator = app.injector.instanceOf[TestJourneyIdGenerator]
  private def paymentItemIdGenerator: TestPaymentItemIdGenerator = app.injector.instanceOf[TestPaymentItemIdGenerator]
  private def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]
  private implicit val request: Request[_] = TdAll.request

  "start Mib journey" in {
    val tdAll = new TdAll {
      private val precachedId = paymentItemIdGenerator.predictNextId() //don't inline
      override lazy val paymentItemId: PaymentItemId = precachedId
      override lazy val journeyId: JourneyId = journeyIdGenerator.predictNextId()
    }

    AuthStub.authorised()
    val journeyId: JourneyId = tdAll.journeyId
    val startJourneyRequest: StartJourneyRequestMib = tdAll.TdJourneyMib.startJourneyRequest
    val startJourneyResponse: StartJourneyResponse = tdAll.TdJourneyMib.startJourneyResponse
    val journey: Journey = tdAll.TdJourneyMib.journeyCreated

    journeyConnector.startJourneyMib(startJourneyRequest).futureValue shouldBe startJourneyResponse
    journeyConnector.find(journeyId).futureValue.value shouldBe journey
  }

  "not start Mib journey if not authorised" in {
    AuthStub.notAuthorised()
    val throwable: Throwable = journeyConnector.startJourneyMib(TdAll.TdJourneyMib.startJourneyRequest).failed.futureValue
    throwable shouldBe an[UpstreamErrorResponse]
    throwable.getMessage should include("""'You do not have access to this service'""")
    throwable.asInstanceOf[UpstreamErrorResponse].statusCode shouldBe 401
  }

  "not start Mib journey if not authenticated" in {
    AuthStub.notAuthenticated()
    val throwable: Throwable = journeyConnector.startJourneyMib(TdAll.TdJourneyMib.startJourneyRequest).failed.futureValue
    throwable shouldBe an[UpstreamErrorResponse]
    throwable.getMessage should include("""'You are not logged in'""")
    throwable.asInstanceOf[UpstreamErrorResponse].statusCode shouldBe 401
  }

  "deserialize request" in {
    val json = TdAll.TdJourneyMib.startJourneyRequestJson
    json.as[StartJourneyRequestMib] shouldBe TdAll.TdJourneyMib.startJourneyRequest
  }

  "deserialize request (amounts as strings)" in {
    val json = TdAll.TdJourneyMib.startJourneyRequestJsonAmountsAsStrings
    json.as[StartJourneyRequestMib] shouldBe TdAll.TdJourneyMib.startJourneyRequest
  }
}
