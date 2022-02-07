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

class DeniedUtrsSpec extends UnitSpec {

  "json serialization/deserialization" in {
    val deniedUtrs = TestData.deniedUtrs1

    val deniedUtrsJson = Json.parse(
      //language=JSON
      """
        {
          "_id" : "denied-utrs-id-123",
          "utrs" : ["utr1","utr2","utr3"],
          "inserted" : "2022-02-04T10:00:24.371"
        }
        """
    )

    Json.toJson(deniedUtrs) shouldBe deniedUtrsJson
    deniedUtrsJson.as[DeniedUtrs] shouldBe deniedUtrs
  }

}
