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

import enumeratum._
import enumformat.EnumFormat
import play.api.libs.json.Format
import play.api.mvc.{PathBindable, QueryStringBindable}
import controllers.ValueClassBinder.{bindableA, valueClassBinder}

import scala.collection.immutable

sealed abstract class TaxType extends EnumEntry {
}

object TaxType {
  implicit val format: Format[TaxType] = EnumFormat(TaxTypes)
  implicit val pathBinder: QueryStringBindable[TaxType] = bindableA(_.toString)
  implicit val taxTypeBinder: PathBindable[TaxType] = valueClassBinder(_.toString)

}

object TaxTypes extends Enum[TaxType] {

  case object P800 extends TaxType
  case object MIB extends TaxType
  case object PNGR extends TaxType
  case object ChildBenefitsRepayments extends TaxType
  case object Safe extends TaxType

  override def values: immutable.IndexedSeq[TaxType] = findValues

}
