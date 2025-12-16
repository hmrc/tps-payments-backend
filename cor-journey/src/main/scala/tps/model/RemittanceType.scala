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

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.Format

import scala.collection.immutable

sealed trait RemittanceType extends EnumEntry {
  val pcipalValue: String
  val screenValue: String
}

object RemittanceType {
  implicit val format: Format[RemittanceType] = tps.utils.EnumFormat(RemittanceTypes)
}

object RemittanceTypes extends Enum[RemittanceType] {
  val values: immutable.IndexedSeq[RemittanceType] = findValues

  case object Unaccompanied extends RemittanceType {
    override val pcipalValue: String = "0"
    override val screenValue: String = "Unaccompanied"
  }

  case object Tax                extends RemittanceType {
    override val pcipalValue: String = "1"
    override val screenValue: String = "Tax"
  }
  case object VatSurcharge       extends RemittanceType {
    override val pcipalValue: String = "2"
    override val screenValue: String = "VAT surcharge"
  }
  case object Penalties          extends RemittanceType {
    override val pcipalValue: String = "4"
    override val screenValue: String = "Penalties"
  }
  case object VatDistraintCosts  extends RemittanceType {
    override val pcipalValue: String = "6"
    override val screenValue: String = "VAT distraint costs"
  }
  case object VatLegalCosts      extends RemittanceType {
    override val pcipalValue: String = "7"
    override val screenValue: String = "VAT legal costs"
  }
  case object VatDefaultInterest extends RemittanceType {
    override val pcipalValue: String = "8"
    override val screenValue: String = "VAT default interest"
  }

}
