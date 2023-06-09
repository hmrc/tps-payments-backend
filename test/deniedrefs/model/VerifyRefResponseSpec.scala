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

package deniedrefs.model

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class VerifyRefResponseSpec extends UnitSpec {

  "json serialization/deserialization" - {

    val testCases = List(
      ("""{"status": "RefDenied"}""", VerifyRefResponse(status = VerifyRefStatuses.RefDenied)),
      ("""{"status": "RefPermitted"}""", VerifyRefResponse(status = VerifyRefStatuses.RefPermitted)),
      ("""{"status": "MissingInformation"}""", VerifyRefResponse(status = VerifyRefStatuses.MissingInformation))
    )

    testCases.foreach { tc =>
      s"test for ${tc._2}" in {
        val json: JsValue = Json.parse(tc._1)
        val verifyRefResponse: VerifyRefResponse = tc._2
        json.as[VerifyRefResponse] shouldBe verifyRefResponse
        Json.toJson(verifyRefResponse) shouldBe json
      }
    }
  }

}
