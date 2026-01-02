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

package tps.journey.model

import play.api.libs.json.{JsObject, Json}
import testsupport.Givens.canEqualJsValue
import testsupport.UnitSpec
import tps.testdata.TdAll
import tps.testdata.util.JsonSyntax.toJsonOps

import scala.reflect.Selectable.reflectiveSelectable

class StartJourneyResponseSpec extends UnitSpec:

  "(de)serialization" in {
    val json: JsObject =
      // language=JSON
      """
        {
          "journeyId": "64886ed616fe8b501cbf0088",
          "nextUrl": "http://localhost:9124/tps-payments/make-payment/mib/64886ed616fe8b501cbf0088"
        }""".asJson

    val s: StartJourneyResponse = TdAll.TdJourneyMib.startJourneyResponse

    Json.toJson(s) shouldBe json
    json.as[StartJourneyResponse] shouldBe s
  }
