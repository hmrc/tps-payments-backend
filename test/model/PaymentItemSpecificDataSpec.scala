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

import play.api.libs.json.{JsResultException, JsString}
import testsupport.UnitSpec
import tps.model._

class PaymentItemSpecificDataSpec extends UnitSpec {

  "PaymentSpecificData get reference utility methods" - {
    "PaymentSpecificDataP800" in {
      PaymentSpecificDataP800("1", "2", "P800", 1).getReference shouldBe "12P8001"
    }
    "PngrSpecificData" in {
      PngrSpecificData("ref", BigDecimal(1), BigDecimal(1), BigDecimal(1)).getReference shouldBe "ref"
    }
    "MibSpecificData" in {
      val psd = MibSpecificData("ref", BigDecimal(1), BigDecimal(1), Some(1))
      psd.getReference shouldBe "ref"
      psd.getAmendmentReference shouldBe Some(1)
    }
    "ChildBenefitSpecificData" in {
      ChildBenefitSpecificData("ref").getReference shouldBe "ref"
    }
    "SaSpecificData" in {
      SaSpecificData("ref").getReference shouldBe "ref"
    }
    "SdltSpecificData" in {
      SdltSpecificData("ref").getReference shouldBe "ref"
    }
    "SafeSpecificData" in {
      SafeSpecificData("ref").getReference shouldBe "ref"
    }
    "CotaxSpecificData" in {
      CotaxSpecificData("ref").getReference shouldBe "ref"
    }
    "NtcSpecificData" in {
      NtcSpecificData("ref").getReference shouldBe "ref"
    }
    "PayeSpecificData" in {
      PayeSpecificData("ref", BigDecimal(1), BigDecimal(1)).getReference shouldBe "ref"
    }
    "NpsSpecificData" in {
      NpsSpecificData("ref", "1", "2", "3", BigDecimal(1)).getReference shouldBe "ref"
    }
    "VatSpecificData" in {
      VatSpecificData("ref", "someRemittanceType").getReference shouldBe "ref"
    }
    "PptSpecificData" in {
      PptSpecificData("ref").getReference shouldBe "ref"
    }

    "Reads[PaymentSpecificData] should throw JsResultException('Could not read PaymentSpecificData') when unknown PaymentSpecificData" in {
      intercept[JsResultException] {
        JsString("invalid").as[PaymentSpecificData]
      }.getMessage.contains("Could not read PaymentSpecificData") shouldBe true
    }
  }
}
