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

import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsResultException, JsString}
import support.UnitSpec
import support.testdata.JsonTestData._
import support.testdata.TestData._

class TpsPaymentsSpec extends UnitSpec {
  "to json" in {
    toJson(tpsPayments) shouldBe tpsPaymentsJson
    toJson(mibPayments) shouldBe mibPaymentsJson
    toJson(childBenefitPayments) shouldBe childBenefitsPaymentsJson
    toJson(saPayments) shouldBe saPaymentsJson
    toJson(sdltPayments) shouldBe sdltPaymentsJson
    toJson(safePayments) shouldBe safePaymentsJson
    toJson(cotaxPayments) shouldBe cotaxPaymentsJson
    toJson(ntcPayments) shouldBe ntcPaymentsJson
    toJson(payePayments) shouldBe payePaymentsJson
    toJson(npsPayments) shouldBe npsPaymentsJson
    toJson(vatPayments) shouldBe vatPaymentsJson
    toJson(pptPayments) shouldBe pptPaymentsJson
  }

  "from json should de-serialise a TpsPayments object with a tax type" in {
    tpsPaymentsJson.as[TpsPayments] shouldBe tpsPayments
    mibPaymentsJson.as[TpsPayments] shouldBe mibPayments
    childBenefitsPaymentsJson.as[TpsPayments] shouldBe childBenefitPayments
    saPaymentsJson.as[TpsPayments] shouldBe saPayments
    sdltPaymentsJson.as[TpsPayments] shouldBe sdltPayments
    safePaymentsJson.as[TpsPayments] shouldBe safePayments
    cotaxPaymentsJson.as[TpsPayments] shouldBe cotaxPayments
    ntcPaymentsJson.as[TpsPayments] shouldBe ntcPayments
    payePaymentsJson.as[TpsPayments] shouldBe payePayments
    npsPaymentsJson.as[TpsPayments] shouldBe npsPayments
    vatPaymentsJson.as[TpsPayments] shouldBe vatPayments
    pptPaymentsJson.as[TpsPayments] shouldBe pptPayments
  }

  "from json should de-serialise json without a tax type and default to P800, e.g. for historical persisted records" in {
    tpsPaymentsJsonWithoutTaxType.as[TpsPayments] shouldBe tpsPayments
  }
}
