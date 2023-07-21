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

package journey

import testsupport.ItSpec
import testsupport.testdata.TestData._

class JourneyRepoSpec extends ItSpec {

  "Count should be 0 with empty repo" in {
    collectionSize shouldBe 0
  }

  "ensure indexes are created" in {
    repo.drop().futureValue
    repo.ensureIndexes().futureValue
    repo.collection.listIndexes().toFuture().futureValue.size shouldBe 6
  }

  "getPayment should throw error when no tpsPayments found" in {
    intercept[Exception] {
      repo.getPayment(journey._id).futureValue
    }.getMessage should include(s"Record with id ${journey._id.value} not found")
  }

  "surfaceModsDataForRecon should find matching mods payments" in {
    repo.surfaceModsDataForRecon(modsLookupChargeRefs).futureValue shouldBe List.empty
    repo.upsert(modsTpsPaymentsNoAmendmentReference).futureValue
    repo.surfaceModsDataForRecon(modsLookupChargeRefs).futureValue shouldBe modsReconLookup
  }

  private def collectionSize: Long = {
    repo.countAll().futureValue
  }
}
