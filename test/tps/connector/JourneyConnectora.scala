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

package tps.connector

import testsupport.ItSpec

class JourneyConnectorSpec extends ItSpec {

  //  "POST/GET journey find/upsert" in {
  //    val journeyIdGenerator = app.injector.instanceOf[JourneyIdGenerator]
  //
  //    val tdAll = new TdAll {
  //      override lazy val journeyId: JourneyId = journeyIdGenerator.nextJourneyId()
  //    }
  //    implicit val request: Request[_] = tdAll.request
  //    val journeyId = tdAll.journeyId
  //    val journeyAfterCreated = tdAll.JourneySa.inSelectTax
  //    val journeyAfterSubmittedTaxType = tdAll.JourneySa.inEnterPayment
  //    journeyAfterSubmittedTaxType.journeyId shouldBe journeyId withClue "sanity check, those should have the same id"
  //    journeyAfterSubmittedTaxType.journeyId shouldBe journeyAfterSubmittedTaxType.journeyId withClue "sanity check, those should have the same id"
  //
  //    journeyDataConnector.findById(journeyAfterCreated.journeyId).futureValue shouldBe None withClue "journey not found as we haven't inserted it yet"
  //    journeyDataConnector.upsert(journeyAfterCreated).futureValue shouldBe (()) withClue "upserting journey"
  //    journeyDataConnector.findById(journeyAfterCreated.journeyId).futureValue shouldBe Some(journeyAfterCreated) withClue "journey should be found"
  //
  //    journeyDataConnector.upsert(journeyAfterSubmittedTaxType).futureValue shouldBe (()) withClue "updating journey with tax type"
  //    journeyDataConnector.findById(journeyId).futureValue shouldBe Some(journeyAfterSubmittedTaxType) withClue "Check journey is updated in Mongo"
  //  }

}
