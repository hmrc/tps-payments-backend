/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.Json.toJson
import support.TpsData._
import support.UnitSpec

class TpsPaymentsSpec extends UnitSpec {
  "to json" in {
    toJson(tpsPayments) shouldBe tpsPaymentsJson
    toJson(mibPayments) shouldBe mibPaymentsJson
  }

  "from json should de-serialise a TpsPayments object with a tax type" in {
    tpsPaymentsJson.as[TpsPayments] shouldBe tpsPayments
    mibPaymentsJson.as[TpsPayments] shouldBe mibPayments
  }

  "from json should de-serialise json without a tax type and default to P800, e.g. for historical persisted records" in {
    tpsPaymentsJsonWithoutTaxType.as[TpsPayments] shouldBe tpsPayments
  }
}
