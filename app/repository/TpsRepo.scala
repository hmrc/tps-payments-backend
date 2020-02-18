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

import javax.inject.{Inject, Singleton}
import model.{TpsId, TpsPayments}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes._
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

@Singleton
final class TpsRepo @Inject() (reactiveMongoComponent: ReactiveMongoComponent, config: RepoConfig)(implicit ec: ExecutionContext)
  extends Repo[TpsPayments, TpsId]("tps-payments", reactiveMongoComponent) {

  override def indexes: Seq[Index] = Seq(
    Index(
      key     = Seq("created" -> IndexType.Ascending),
      name    = Some("createdIdx"),
      options = BSONDocument("expireAfterSeconds" -> config.expireMongo.toSeconds)
    )
  )

  def findPayment(tpsId: TpsId): Future[Option[TpsPayments]] = findById(tpsId)

}
