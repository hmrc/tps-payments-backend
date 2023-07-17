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

package model

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

import journey.JourneyRepo
import play.api.libs.json.{JsObject, Json}
import play.api.libs.json.Json.toJson
import testsupport.UnitSpec
import testsupport.testdata.JsonTestData._
import testsupport.testdata.TestData._
import tps.journey.model.Journey
import tps.model.Navigation
import tps.testdata.TdAll
import tps.testdata.util.JsonSyntax.toJsonOps
import tps.testdata.util.ResourceReader

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

class JourneySpec extends UnitSpec {

  "generate journey jsons" ignore {

    TdAll.allTdJourneysWithJson.foreach { t =>
      val content = Json.prettyPrint(Json.toJson(t._1))
      val rootPath = "/dbox/projects/hmrcdigital/tps-payments-backend/cor-journey-test-data/src/main/resources"
      val path = s"$rootPath${t._2.resourcePath}"
      println(s"generating $path...")
      Files.createDirectories(Paths.get(path).getParent)
      Files.write(
        Paths.get(path),
        content.getBytes(StandardCharsets.UTF_8)
      )
    }

  }

  case class TestCase(journey: Journey, json: JsObject, testCaseName: String)

  val testCases: List[TestCase] = TdAll.allTdJourneysWithJson
    .map { t =>
      val journey = t._1
      val json = t._2.json
      val testCaseName = t._2.simpleName
      TestCase(journey      = journey, json = json, testCaseName = testCaseName)
    }

  "serialize" - testCases.foreach { tc =>
    tc.testCaseName in {
      toJson(tc.journey) shouldBe tc.json
    }
  }

  "deserialize" - testCases.foreach { tc =>
    tc.testCaseName in {
      tc.json.as[Journey] shouldBe tc.journey
    }
  }

  "mongo requires time formatted in a special way" in {

    val journeyInMongoJson = ResourceReader.read(
      "/tps/journeysinmongo/journey-childbenefit.json"
    ).asJson

    val journey = TdAll.TdJourneyChildBenefit.journeyReceivedNotification
    JourneyRepo.formatMongo.writes(journey) shouldBe journeyInMongoJson
    JourneyRepo.formatMongo.reads(journeyInMongoJson).asOpt.value shouldBe journey
  }

  "mongo format can read legacy journey with old time format" in {

    val legacyJourneyInMongoJson = ResourceReader.read(
      "/tps/journeysinmongo/journey-childbenefit-legacy-time-format.json"
    ).asJson

    val journey = TdAll.TdJourneyChildBenefit.journeyReceivedNotification
    JourneyRepo.formatMongo.reads(legacyJourneyInMongoJson).asOpt.value shouldBe journey
  }

  "mongo format can read legacy journey without navigation data" in {

    val legacyJourneyInMongoJson = ResourceReader.read(
      "/tps/journeysinmongo/journey-childbenefit-legacy-no-navigation.json"
    ).asJson

    val journey = TdAll.TdJourneyChildBenefit.journeyReceivedNotification.copy(navigation = Navigation(
      back     = "dummy", reset = "dummy", finish = "dummy", callback = "dummy"
    ))
    JourneyRepo.formatMongo.reads(legacyJourneyInMongoJson).asOpt.value shouldBe journey
  }

  "mongo format can read legacy journey without journeyStatus" in {
    val legacyJourneyInMongoJson = ResourceReader.read(
      "/tps/journeysinmongo/journey-childbenefit-legacy-no-journey-status.json"
    ).asJson
    val journey = TdAll.TdJourneyChildBenefit.journeyReceivedNotification
    JourneyRepo.formatMongo.reads(legacyJourneyInMongoJson).asOpt.value shouldBe journey withClue "journeyState for legacy journeys is assumed to be ReceivedNotification"
  }

}
