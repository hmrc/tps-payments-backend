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

package repository

import model.JourneyId
import model.pcipal.PcipalSessionId
import support.ItSpec
import support.testdata.TestData._

class JourneyRepoSpec extends ItSpec {

  "Count should be 0 with empty repo" in {
    collectionSize shouldBe 0
  }

  "ensure indexes are created" in {
    repo.drop().futureValue
    repo.ensureIndexes().futureValue
    repo.collection.listIndexes().toFuture().futureValue.size shouldBe 3
  }

  "insert and find a record" in {
    Option(repo.upsert(tpsPayments).futureValue.getUpsertedId).isDefined shouldBe true
    repo.findPayment(id).futureValue shouldBe Some(tpsPayments)
  }

  "getPayment should throw error when no tpsPayments found" in {
    intercept[Exception] {
      repo.getPayment(tpsPayments._id).futureValue
    }.getMessage should include(s"Record with id ${tpsPayments._id.value} not found")
  }

  "findByPcipalSessionId should throw error when more than one payment found" in {
    Option(repo.upsert(tpsPaymentsWithPcipalData).futureValue.getUpsertedId).isDefined shouldBe true
    Option(repo.upsert(tpsPaymentsWithPcipalData.copy(_id = JourneyId("session-48c978bb-64b6-4a00-a1f1-51e267some-new-one"))).futureValue.getUpsertedId).isDefined shouldBe true
    intercept[Exception] {
      repo.findByPcipalSessionId(PcipalSessionId("48c978bb")).futureValue
    }.getMessage should include("Found 2 records with id 48c978bb.")
  }

  "insert and find an mib tps payment" in {
    mibPayments.payments.headOption.value.paymentSpecificData.getReference shouldBe "chargeReference"
    Option(repo.upsert(mibPayments).futureValue.getUpsertedId).isDefined shouldBe true
    repo.findPayment(mibPayments._id).futureValue shouldBe Some(mibPayments)
  }

  "findPaymentItem should optionally find the matching payment item" in {
    repo.findPaymentItem(paymentItemId).futureValue shouldBe None
    repo.upsert(tpsPayments).futureValue
    repo.findPaymentItem(paymentItemId).futureValue shouldBe Some(tpsPayments.payments.headOption.value)
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
