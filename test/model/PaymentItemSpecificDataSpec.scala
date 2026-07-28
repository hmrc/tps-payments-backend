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

  "PaymentSpecificData" - {

    val testReference = "someReference"

    "PngrSpecificData" - {

      val testPngrSpecificData = PngrSpecificData(testReference)

      "getReference should return the chargeReference" in {
        testPngrSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the chargeReference" in {
        testPngrSpecificData.getRawReference shouldBe testReference
      }

      "searchTag should return the chargeReference" in {
        testPngrSpecificData.searchTag shouldBe testReference
      }
    }

    "MibSpecificData" - {

      val testMibSpecificData = MibSpecificData(testReference, BigDecimal(1), BigDecimal(1), Some(1))

      "getReference should return the chargeReference" in {
        testMibSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the chargeReference" in {
        testMibSpecificData.getRawReference shouldBe testReference
      }

      "searchTag should return the chargeReference" in {
        testMibSpecificData.searchTag shouldBe testReference
      }

      "getAmendmentReference should return the amendmentReference" in {
        testMibSpecificData.getAmendmentReference shouldBe Some(1)
      }
    }

    "ChildBenefitSpecificData" - {

      val testChildBenefitSpecificData = ChildBenefitSpecificData(testReference)

      "getReference should return the childBenefitYReference" in {
        testChildBenefitSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the childBenefitYReference" in {
        testChildBenefitSpecificData.getRawReference shouldBe testReference
      }

      "searchTag should return the childBenefitYReference" in {
        testChildBenefitSpecificData.searchTag shouldBe testReference
      }
    }

    "SaSpecificData" - {

      val testSaSpecificData = SaSpecificData("1234567895K")

      "getReference should return the saReference as is" in {
        testSaSpecificData.getReference shouldBe "1234567895K"
      }

      "getRawReference should return the saReference with last character dropped" in {
        testSaSpecificData.getRawReference shouldBe "1234567895"
      }

      "searchTag should return the saReference" - {
        "without any K when there is one" in {
          testSaSpecificData.searchTag shouldBe "1234567895"
        }
        "without any K when it's randomly in the middle of the string" in {
          SaSpecificData("123K4567895").searchTag shouldBe "1234567895"
        }
        "without any K when it's randomly at the start of the string" in {
          SaSpecificData("K1234567895").searchTag shouldBe "1234567895"
        }
      }
    }

    "SdltSpecificData" - {

      val testSdltSpecificData = SdltSpecificData(testReference)

      "getReference should return the sdltReference" in {
        testSdltSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the sdltReference" in {
        testSdltSpecificData.getRawReference shouldBe testReference
      }

      "searchTag should return the sdltReference" in {
        testSdltSpecificData.searchTag shouldBe testReference
      }
    }

    "SafeSpecificData" - {

      val testSafeSpecificData = SafeSpecificData(testReference)

      "getReference should return the safeReference" in {
        testSafeSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the safeReference" in {
        testSafeSpecificData.getRawReference shouldBe testReference
      }

      "searchTag should return the safeReference" in {
        testSafeSpecificData.searchTag shouldBe testReference
      }
    }

    "CotaxSpecificData" - {

      val testCotaxSpecificData = CotaxSpecificData(testReference)

      "getReference should return the cotaxReference" in {
        testCotaxSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the cotaxReference with the last 7 characters dropped" in {
        testCotaxSpecificData.getRawReference shouldBe "someRe"
      }

      "searchTag should return the cotaxReference with the last 7 characters dropped" in {
        testCotaxSpecificData.searchTag shouldBe "someRe"
      }
    }

    "NtcSpecificData" - {

      val testNtcSpecificData = NtcSpecificData(testReference)

      "getReference should return the ntcReference" in {
        testNtcSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the ntcReference with the last 8 characters dropped" in {
        testNtcSpecificData.getRawReference shouldBe "someR"
      }

      "searchTag should return the ntcReference with the last 8 characters dropped" in {
        testNtcSpecificData.searchTag shouldBe "someR"
      }
    }

    "PayeSpecificData" - {

      val testPayeSpecificData = PayeSpecificData(testReference, BigDecimal(1), BigDecimal(1))

      "getReference should return the payeReference" in {
        testPayeSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the payeReference with the last four characters dropped" in {
        testPayeSpecificData.getRawReference shouldBe "someRefer"
      }

      "searchTag should return the payeReference with the last four characters dropped" in {
        testPayeSpecificData.searchTag shouldBe "someRefer"
      }
    }

    "NpsSpecificData" - {

      val testNpsSpecificData = NpsSpecificData(testReference, "1", "2", "3", BigDecimal(1))

      "getReference should return the npsReference" in {
        testNpsSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the npsReference with the last two characters dropped" in {
        testNpsSpecificData.getRawReference shouldBe "someReferen"
      }

      "searchTag should return the npsReference" in {
        testNpsSpecificData.searchTag shouldBe "someReference"
      }
    }

    "VatSpecificData" - {

      val testVatSpecificData = VatSpecificData(testReference, "someRemittanceType")

      "getReference should return the vatReference" in {
        testVatSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the vatReference with the last four characters dropped" in {
        testVatSpecificData.getRawReference shouldBe "someRefer"
      }

      "searchTag should return the vatReference" in {
        testVatSpecificData.searchTag shouldBe "someReference"
      }
    }

    "PptSpecificData" - {

      val testPptSpecificData = PptSpecificData(testReference)

      "getReference should return the pptReference" in {
        testPptSpecificData.getReference shouldBe testReference
      }

      "getRawReference should return the pptReference" in {
        testPptSpecificData.getRawReference shouldBe testReference
      }

      "searchTag should return the pptReference" in {
        testPptSpecificData.searchTag shouldBe testReference
      }
    }

    "Reads[PaymentSpecificData] should throw JsResultException('Could not read PaymentSpecificData') when unknown PaymentSpecificData" in {
      intercept[JsResultException] {
        JsString("invalid").as[PaymentSpecificData]
      }.getMessage.contains("Could not read PaymentSpecificData") shouldBe true
    }
  }
}
