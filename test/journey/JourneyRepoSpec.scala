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

import org.mongodb.scala.ObservableFuture
import testsupport.Givens.canEqualList
import testsupport.ItSpec
import testsupport.testdata.TestData.*
import tps.journey.model.{Journey, JourneyId}
import tps.testdata.TdAll

class JourneyRepoSpec extends ItSpec {

  "Count should be 0 with empty repo" in {
    collectionSize shouldBe 0
  }

  "ensure indexes are created" in {
    journeyRepo.drop().futureValue
    journeyRepo.ensureIndexes().futureValue
    journeyRepo.collection.listIndexes().toFuture().futureValue.size shouldBe 7
  }

  "getPayment should throw error when no tpsPayments found" in {
    intercept[Exception] {
      journeyRepo.getPayment(journey._id).futureValue
    }.getMessage should include(s"Record with id ${journey._id.value} not found")
  }

  "surfaceModsDataForRecon should find matching mods payments" in {
    journeyRepo.surfaceModsDataForRecon(modsLookupChargeRefs).futureValue shouldBe List.empty
    journeyRepo.upsert(modsTpsPaymentsNoAmendmentReference).futureValue
    journeyRepo.surfaceModsDataForRecon(modsLookupChargeRefs).futureValue shouldBe modsReconLookup
  }

  ".findBySearchTag" - {

    "should return a journey when there is one when searchTag matches one of the references passed in" in {
      val testJourney    = TdAll.TdJourneySa.journeyReceivedNotification
      val testReferences = Seq("1234567895")

      insertData(testJourney)

      val result: Seq[Journey] = journeyRepo.findBySearchTag(testReferences).futureValue
      result shouldEqual Seq(testJourney)
    }

    "should return multiple journeys when there are multiple, matching the same searchTag" in {
      val testJourney        = TdAll.TdJourneySa.journeyReceivedNotification
      val anotherTestJourney = testJourney.copy(_id = JourneyId("something-else"))
      val testReferences     = Seq("1234567895")

      insertData(testJourney, anotherTestJourney)

      val result: Seq[Journey] = journeyRepo.findBySearchTag(testReferences).futureValue
      result shouldEqual Seq(testJourney, anotherTestJourney)
    }

    "should return an empty Sequence when no journeys are found for the searchTag" in {
      val result: Seq[Journey] = journeyRepo.findBySearchTag(Seq("1234567895")).futureValue
      result shouldEqual Seq.empty[Journey]
    }

    "should return an empty Sequence when the look up reference for SA has a K on the end, but the search tag does not" in {
      val testJourney    = TdAll.TdJourneySa.journeyReceivedNotification
      val testReferences = Seq("1234567895K")

      insertData(testJourney)

      val result: Seq[Journey] = journeyRepo.findBySearchTag(testReferences).futureValue
      result shouldEqual Seq.empty[Journey]
    }
  }

  private def collectionSize: Long = journeyRepo.countAll().futureValue

  private def insertData(journey: Journey*) =
    journey.flatMap(j => Some(journeyRepo.upsert(j).futureValue.wasAcknowledged())).forall(identity) shouldBe true

}
