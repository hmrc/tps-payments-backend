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

import journeysupport.TestJourneyIdGenerator
import play.api.mvc.Request
import testsupport.ItSpec
import testsupport.stubs.AuthStub
import tps.journey.model.JourneyId
import tps.testdata.TdAll
import uk.gov.hmrc.http.UpstreamErrorResponse

@SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
class JourneyConnectorSpec extends ItSpec {

  def journeyIdGenerator = app.injector.instanceOf[TestJourneyIdGenerator]
  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]
  implicit val request: Request[_] = TdAll.request

  "find and upsert journey in all possible state" - TdAll.allTdJourneysWithJson.map{ t =>
    val journey = t._1
    val testCaseName = t._2.simpleName
    testCaseName in {
      AuthStub.authorised()
      journeyConnector.upsert(journey).futureValue shouldBe (()) withClue "upserting journey"
      journeyConnector.find(journey.journeyId).futureValue shouldBe Some(journey) withClue "journey should be found"
    }
  }

  "find should return None if no journey at db" in {
    AuthStub.authorised()
    journeyConnector.find(journeyIdGenerator.nextId()).futureValue shouldBe None withClue "journey not found as not inserted"
  }

  "find unauthorised" in {
    AuthStub.notAuthorised()
    val throwable: Throwable = journeyConnector.find(TdAll.journeyId).failed.futureValue
    throwable shouldBe an[UpstreamErrorResponse]
    throwable should have message """GET of 'http://localhost:19001/tps-payments-backend/journey/64886ed616fe8b501cbf0088' returned 401. Response body: 'You do not have access to this service'"""
    throwable.asInstanceOf[UpstreamErrorResponse].statusCode shouldBe 401
  }

  "find unauthenticated" in {
    AuthStub.notAuthenticated()
    val throwable: Throwable = journeyConnector.find(TdAll.journeyId).failed.futureValue
    throwable shouldBe an[UpstreamErrorResponse]
    throwable should have message """GET of 'http://localhost:19001/tps-payments-backend/journey/64886ed616fe8b501cbf0088' returned 401. Response body: 'You are not logged in'"""
    throwable.asInstanceOf[UpstreamErrorResponse].statusCode shouldBe 401
  }

  "upsert unauthorised" in {
    AuthStub.notAuthorised()
    val throwable: Throwable = journeyConnector.upsert(TdAll.TdJourneyCotax.journeyAtPciPal).failed.futureValue
    throwable shouldBe an[UpstreamErrorResponse]
    throwable should have message """POST of 'http://localhost:19001/tps-payments-backend/journey' returned 401. Response body: 'You do not have access to this service'"""
    throwable.asInstanceOf[UpstreamErrorResponse].statusCode shouldBe 401
  }

  "upsert unauthenticated" in {
    AuthStub.notAuthenticated()
    val throwable: Throwable = journeyConnector.upsert(TdAll.TdJourneyCotax.journeyAtPciPal).failed.futureValue
    throwable shouldBe an[UpstreamErrorResponse]
    throwable should have message """POST of 'http://localhost:19001/tps-payments-backend/journey' returned 401. Response body: 'You are not logged in'"""
    throwable.asInstanceOf[UpstreamErrorResponse].statusCode shouldBe 401
  }

}
