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

package tps.model

import enumeratum._
import play.api.libs.json.Format
import tps.utils.EnumFormat

import scala.collection.immutable

sealed abstract class TaxType extends EnumEntry

object TaxType {
  implicit val format: Format[TaxType] = EnumFormat(TaxTypes)
}

object TaxTypes extends Enum[TaxType] {

  case object P800 extends TaxType
  case object MIB extends TaxType
  case object PNGR extends TaxType
  case object ChildBenefitsRepayments extends TaxType
  case object Sa extends TaxType
  case object Sdlt extends TaxType
  case object Safe extends TaxType
  case object Cotax extends TaxType
  case object Ntc extends TaxType
  case object Paye extends TaxType
  case object Nps extends TaxType
  case object Vat extends TaxType
  case object Ppt extends TaxType

  override def values: immutable.IndexedSeq[TaxType] = findValues

}
