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
import repository.JourneyRepo
import support.UnitSpec
import support.testdata.JsonTestData._
import support.testdata.TestData._
import tps.model.Journey

class JourneySpec extends UnitSpec {
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
    tpsPaymentsJson.as[Journey] shouldBe tpsPayments
    mibPaymentsJson.as[Journey] shouldBe mibPayments
    childBenefitsPaymentsJson.as[Journey] shouldBe childBenefitPayments
    saPaymentsJson.as[Journey] shouldBe saPayments
    sdltPaymentsJson.as[Journey] shouldBe sdltPayments
    safePaymentsJson.as[Journey] shouldBe safePayments
    cotaxPaymentsJson.as[Journey] shouldBe cotaxPayments
    ntcPaymentsJson.as[Journey] shouldBe ntcPayments
    payePaymentsJson.as[Journey] shouldBe payePayments
    npsPaymentsJson.as[Journey] shouldBe npsPayments
    vatPaymentsJson.as[Journey] shouldBe vatPayments
    pptPaymentsJson.as[Journey] shouldBe pptPayments
  }

  "mongo writes" in {
    JourneyRepo.formatMongo.writes((tpsPayments)) shouldBe tpsPaymentsMongoJson
  }

  "mongo reads" in {
    tpsPaymentsMongoJson.as[Journey](JourneyRepo.formatMongo) shouldBe tpsPayments
  }

  "mongo legacy reads" in {
    tpsPaymentsMongoLegacyJson.as[Journey](JourneyRepo.formatMongo) shouldBe tpsPayments
  }

}
