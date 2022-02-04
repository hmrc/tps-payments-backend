/*
 * Copyright 2022 HM Revenue & Customs
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

package deniedutrs.model

import play.api.libs.json.Json
import support.{TestData, UnitSpec}

class VerifyUtrRequestSpec extends UnitSpec {

  "json serialization/deserialization" in {
    val verifyUtrRequest = TestData.verifyUtrRequest

    val verifyUtrRequestJson = Json.parse(
      //language=JSON
      """
         {
          "utr": ["utr1"]
          }
        """)

    Json.toJson(verifyUtrRequest) shouldBe verifyUtrRequestJson
    verifyUtrRequestJson.as[VerifyUtrRequest] shouldBe verifyUtrRequest
  }
}
