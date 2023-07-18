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

package deniedrefs

import com.mongodb.client.model.Sorts
import deniedrefs.model.{DeniedRefs, DeniedRefsId}
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import repository.Repo
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
    mongoComponent: MongoComponent
)(implicit ec: ExecutionContext)
  extends Repo[DeniedRefsId, DeniedRefs](
    collectionName = "denied-refs",
    mongoComponent = mongoComponent,
    indexes        = DeniedRefsRepo.indexes(),
    extraCodecs    = Seq.empty,
    replaceIndexes = true
  ) {

  def findLatestDeniedRefsId(): Future[Option[DeniedRefsId]] = findLatestDeniedRefs().map(_.map(_._id))

  /**
   * Projection is used (i.e. slice("_id", 1) ) to limit the number of records returned to just one.
   * Projection is also used (i.e. slice("refs", 1) ) to limit the number of refs returned to just one.
   * We don't need them and it can introduce performance issue if there are lots in list of refs inside DeniedRefs
   * Don't remove this... unless you know what you're doing ;)
   */
  protected[deniedrefs] def findLatestDeniedRefs(): Future[Option[DeniedRefs]] = {
    collection.find()
      .sort(Sorts.descending(inserted))
      .projection(slice("_id", 1))
      .projection(slice("refs", 1))
      .headOption()
  }

  private lazy val inserted = "inserted"
}
