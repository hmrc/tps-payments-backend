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
import tps.journey.model.JourneyId
import tps.model.PaymentItemId
import tps.testdata.TdAll
import uk.gov.hmrc.http.UpstreamErrorResponse

@SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
class StartJourneyControllerSpec extends ItSpec {

  private def journeyIdGenerator: TestJourneyIdGenerator = app.injector.instanceOf[TestJourneyIdGenerator]
  private def paymentItemIdGenerator: TestPaymentItemIdGenerator = app.injector.instanceOf[TestPaymentItemIdGenerator]

  private def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]
  private implicit val request: Request[_] = TdAll.request

  "start Mib journey" in {

    val tdAll = new TdAll {
      private val precatchedId = paymentItemIdGenerator.predictNextId() //don't inline
      override lazy val paymentItemId: PaymentItemId = precatchedId
      override lazy val journeyId: JourneyId = journeyIdGenerator.predictNextId()
    }

    AuthStub.authorised()
    val journeyId: JourneyId = tdAll.journeyId
    journeyConnector.startMibOrPngrJourney(tdAll.JourneyMib.startJourneyRequest).futureValue shouldBe journeyId
    journeyConnector.find(journeyId).futureValue.value shouldBe tdAll.JourneyMib.journeyAfterCreated
  }

  "start Mib journey if not authorised" in {
    AuthStub.notAuthorised()
    val throwable: Throwable = journeyConnector.startMibOrPngrJourney(TdAll.JourneyMib.startJourneyRequest).failed.futureValue
    throwable shouldBe an[UpstreamErrorResponse]
    throwable.getMessage should include("""'You do not have access to this service'""")
    throwable.asInstanceOf[UpstreamErrorResponse].statusCode shouldBe 401
  }

  "start Mib journey if not authenticated" in {
    AuthStub.notAuthenticated()
    val throwable: Throwable = journeyConnector.startMibOrPngrJourney(TdAll.JourneyMib.startJourneyRequest).failed.futureValue
    throwable shouldBe an[UpstreamErrorResponse]
    throwable.getMessage should include("""'You are not logged in'""")
    throwable.asInstanceOf[UpstreamErrorResponse].statusCode shouldBe 401
  }

  "start Pngr journey" in {

    val tdAll = new TdAll {
      private val precatchedId = paymentItemIdGenerator.predictNextId() //don't inline
      override lazy val paymentItemId: PaymentItemId = precatchedId
      override lazy val journeyId: JourneyId = journeyIdGenerator.predictNextId()
    }

    AuthStub.authorised()
    val journeyId: JourneyId = tdAll.journeyId
    journeyConnector.startMibOrPngrJourney(tdAll.JourneyPngr.startJourneyRequest).futureValue shouldBe journeyId
    journeyConnector.find(journeyId).futureValue.value shouldBe tdAll.JourneyPngr.journeyAfterCreated
  }

  "start Pngr journey if not authorised" in {
    AuthStub.notAuthorised()
    val throwable: Throwable = journeyConnector.startMibOrPngrJourney(TdAll.JourneyPngr.startJourneyRequest).failed.futureValue
    throwable shouldBe an[UpstreamErrorResponse]
    throwable.getMessage should include("""'You do not have access to this service'""")
    throwable.asInstanceOf[UpstreamErrorResponse].statusCode shouldBe 401
  }

  "start Pngr journey if not authenticated" in {
    AuthStub.notAuthenticated()
    val throwable: Throwable = journeyConnector.startMibOrPngrJourney(TdAll.JourneyPngr.startJourneyRequest).failed.futureValue
    throwable shouldBe an[UpstreamErrorResponse]
    throwable.getMessage should include("""'You are not logged in'""")
    throwable.asInstanceOf[UpstreamErrorResponse].statusCode shouldBe 401
  }

}
