/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.ValueClassBinder._
import enumeratum._
import enumformat.EnumFormat
import play.api.libs.json.Format
import play.api.mvc.{PathBindable, QueryStringBindable}

import scala.collection.immutable

object StatusType {
  implicit val format: Format[StatusType] = EnumFormat(StatusTypes)
  implicit val pathBinder: QueryStringBindable[StatusType] = bindableA(_.toString)
  implicit val statusBinder: PathBindable[StatusType] = valueClassBinder(_.toString)

}

sealed abstract class StatusType extends EnumEntry

object StatusTypes extends Enum[StatusType] {

  val values: immutable.IndexedSeq[StatusType] = findValues

  def forCode(code: String): Option[StatusType] = values.find(_.toString == code)

  case object validated extends StatusType

  case object failed extends StatusType

}
