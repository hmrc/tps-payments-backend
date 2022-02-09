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

import model.{DeniedRefs, DeniedRefsId}
import _root_.model.Reference
import play.api.libs.json.{Json, Reads}
import play.modules.reactivemongo.ReactiveMongoComponent
import play.api.libs.json._
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import repository.Repo

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
final class DeniedRefsRepo @Inject() (reactiveMongoComponent: ReactiveMongoComponent)(implicit ec: ExecutionContext)
  extends Repo[DeniedRefs, DeniedRefsId]("denied-refs", reactiveMongoComponent) {

  def upsert(deniedRefs: DeniedRefs): Future[WriteResult] = upsert(deniedRefs._id, deniedRefs)

  private val deniedRefsReads: Reads[DeniedRefsId] = (__ \ "_id").read[DeniedRefsId]

  def findLatestDeniedRefsId(): Future[Option[DeniedRefsId]] = {
    val projection = Json.obj("_id" -> 1) //a projection so we don't pull whole document for performance reasons
    collection
      .find(
        selector   = Json.obj(),
        projection = Some(projection)
      )
      .sort(Json.obj(inserted -> -1))
      .one[DeniedRefsId](ReadPreference.primaryPreferred)(deniedRefsReads, implicitly)
  }

  override def indexes: Seq[Index] = Seq(
    Index(
      key    = Seq(inserted -> IndexType.Ascending),
      name   = Some(inserted),
      unique = true
    )
  )

  private lazy val inserted = "inserted"
}
