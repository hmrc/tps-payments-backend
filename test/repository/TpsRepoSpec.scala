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

package repository

import play.api.libs.json.Json
import reactivemongo.api.commands.UpdateWriteResult
import support.{ItSpec, TpsData}

class TpsRepoSpec extends ItSpec {

  val repo: TpsRepo = injector.instanceOf[TpsRepo]

  override def beforeEach(): Unit = {
    super.beforeEach()
    val remove = repo.removeAll().futureValue
  }

  "Count should be 0 with empty repo" in {
    collectionSize shouldBe 0
  }

  "ensure indexes are created" in {

    val remove = repo.drop.futureValue
    val ensure = repo.ensureIndexes.futureValue
    repo.collection.indexesManager.list().futureValue.size shouldBe 3
  }

  "insert a record" in {
    val upserted: UpdateWriteResult = repo.upsert(TpsData.id, TpsData.tpsPayments).futureValue
    upserted.n shouldBe 1
  }

  private def collectionSize: Int = {
    repo.count(Json.obj()).futureValue
  }

}
