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

import play.api.libs.json.JsResultException
import play.api.libs.json.Json.toJson
import support.testdata.TestData._
import support.UnitSpec

class TpsPaymentRequestSpecItem extends UnitSpec {
  "to json should serialise to json" in {
    toJson(tpsPaymentRequest) shouldBe paymentRequestJson
  }

  "from json should de-serialise from json" in {
    paymentRequestJson.as[TpsPaymentRequest] shouldBe tpsPaymentRequest
  }

  "to json should serialise to json pngr" in {
    toJson(tpsPaymentRequestPngr) shouldBe paymentRequestPngrJson
  }

  "from json should de-serialise from json pngr" in {
    paymentRequestPngrJson.as[TpsPaymentRequest] shouldBe tpsPaymentRequestPngr
  }

  "to json should serialise to json mib" in {
    toJson(tpsPaymentRequestMib) shouldBe paymentRequestMibJson
  }

  "from json should de-serialise from json mib" in {
    paymentRequestMibJson.as[TpsPaymentRequest] shouldBe tpsPaymentRequestMib
  }

  "from json should error for an invalid tax type" in {
    intercept[JsResultException] {
      invalidPaymentRequestJson.as[TpsPaymentRequest]
    }.getMessage.contains("Unknown TaxTypes") shouldBe true
  }

}
