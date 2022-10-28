/*
 * Copyright 2022 HM Revenue & Customs
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

package deniedrefs

import com.mongodb.client.model.Sorts
import deniedrefs.model.{DeniedRefs, DeniedRefsId}
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import repository.{Repo, RepoConfig}
import uk.gov.hmrc.mongo.MongoComponent

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object DeniedRefsRepo {
  def indexes(): Seq[IndexModel] = Seq(
    IndexModel(
      keys         = Indexes.ascending("inserted"),
      indexOptions = IndexOptions().unique(true).name("inserted")
    )
  )
}

@Singleton
final class DeniedRefsRepo @Inject() (
    mongoComponent: MongoComponent,
    config:         RepoConfig
)(implicit ec: ExecutionContext)
  extends Repo[DeniedRefsId, DeniedRefs](
    collectionName = "denied-refs",
    mongoComponent = mongoComponent,
    indexes        = DeniedRefsRepo.indexes(),
    extraCodecs    = Seq.empty,
    replaceIndexes = true
  ) {

  def findLatestDeniedRefsId(): Future[Option[DeniedRefsId]] = collection
    .find()
    .sort(Sorts.descending(inserted))
    .headOption()
    .map(_.map(_._id))

  private lazy val inserted = "inserted"
}
