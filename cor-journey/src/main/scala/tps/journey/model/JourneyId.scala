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

package tps.journey.model

import org.bson.types.ObjectId
import play.api.libs.json._
import play.api.mvc.PathBindable
import tps.model.repo.Id
import tps.utils.ValueClassBinder.valueClassBinder

final case class JourneyId(value: String) extends AnyVal with Id derives CanEqual

object JourneyId {
  implicit val format: Format[JourneyId]                = Json.valueFormat[JourneyId]
  implicit val journeyIdBinder: PathBindable[JourneyId] = valueClassBinder(_.value)
  def fresh(): JourneyId                                = JourneyId(ObjectId.get().toHexString)
}
