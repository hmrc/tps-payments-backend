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

import deniedrefs.TdDeniedRefs
import play.api.libs.json.Json
import testsupport.UnitSpec

class DeniedRefsSpec extends UnitSpec {

  "json serialization/deserialization" in {
    val deniedRefs = TdDeniedRefs.deniedRefs1

    val deniedRefsJson = Json.parse(
      // language=JSON
      """
        {
          "_id" : "denied-refs-id-123",
          "refs" : ["REF1","REF2","REF3"],
          "inserted" : "2022-02-04T10:00:24.371"
        }
        """
    )

    Json.toJson(deniedRefs) shouldBe deniedRefsJson
    deniedRefsJson.as[DeniedRefs] shouldBe deniedRefs
  }

}
