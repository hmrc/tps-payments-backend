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

import play.api.libs.json.{JsValue, Json}
import testsupport.UnitSpec
import tps.model.TaxTypes
import tps.testdata.TdAll

class JourneyStateSpec extends UnitSpec {

  private val testCases: List[(JourneyState, String)] = List(
    JourneyState.Started -> """{"Started":{}}""",
    JourneyState.EnterPayment(TaxTypes.Sa) -> """{"EnterPayment":{"taxType":"Sa"}}""",
    JourneyState.EditPayment(TdAll.paymentItemId) -> """{"EditPayment":{"paymentItemId":"64897aee16fe8b501cbf008a"}}""",
    JourneyState.AtPciPal -> """{"AtPciPal":{}}""",
    JourneyState.Rejected -> """{"Rejected":{}}""",
    JourneyState.ResetByPciPal -> """{"ResetByPciPal":{}}""",
    JourneyState.FinishedByPciPal -> """{"FinishedByPciPal":{}}""",
    JourneyState.BackByPciPal -> """{"BackByPciPal":{}}""",
    JourneyState.ReceivedNotification -> """{"ReceivedNotification":{}}"""
  )

  testCases.foreach { tc =>
    val journeyState = tc._1
    val jsonString = tc._2
    s"de/serialize ${journeyState.toString}" in {
      val json: JsValue = Json.parse(jsonString)
      Json.toJson(journeyState: JourneyState) shouldBe json
      json.as[JourneyState] shouldBe journeyState
    }
  }

  "legacy journey states should be deserialized" - {
    val testCases = List(
      """{"Landing":{}}""" -> JourneyState.Started,
      """{"BasketNotEmpty":{}}""" -> JourneyState.Started
    )

    testCases.foreach { tc =>
      val jsonString: String = tc._1
      val journeyState: JourneyState = tc._2
      s"deserialize $jsonString" in {
        val json: JsValue = Json.parse(jsonString)
        json.as[JourneyState] shouldBe journeyState
      }
    }
  }
}
