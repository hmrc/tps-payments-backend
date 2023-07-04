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
import play.api.mvc.{PathBindable, QueryStringBindable}

import scala.collection.immutable

sealed trait TaxType extends EnumEntry {
  def pcipalProductionClientId: String
  def pcipalTestClientId: String
  def screenValue: String

  def clientId(usePcipalTestSettings: Boolean): String =
    if (usePcipalTestSettings) pcipalTestClientId else pcipalProductionClientId
}

object TaxType {
  implicit val format: Format[TaxType] = tps.utils.EnumFormat(TaxTypes)
  implicit val pathBinder: QueryStringBindable[TaxType] = tps.utils.ValueClassBinder.bindableA(_.toString)
  implicit val taxTypeBinder: PathBindable[TaxType] = tps.utils.ValueClassBinder.valueClassBinder(_.toString)

}

object TaxTypes extends Enum[TaxType] {
  private val genericPcipalProductionClientId = "TPSETMP"
  private val genericPcipalTestClientId = "TPSP800"
  val values: immutable.IndexedSeq[TaxType] = findValues

  case object ChildBenefitsRepayments extends TaxType {
    override val pcipalProductionClientId: String = "CBCE"
    override val pcipalTestClientId: String = "CBCE"
    override val screenValue: String = "Repay Child Benefit overpayments"
  }

  case object Cotax extends TaxType {
    override val pcipalProductionClientId: String = "COPL"
    override val pcipalTestClientId: String = "COPD"
    override val screenValue: String = "COTAX"
  }

  case object MIB extends TaxType {
    override val pcipalProductionClientId: String = "MBML"
    override val pcipalTestClientId: String = "MPCE"
    override val screenValue: String = "MIB"
  }

  case object Nps extends TaxType {
    override val pcipalProductionClientId: String = "NPPL"
    override val pcipalTestClientId: String = "NPPD"
    override val screenValue: String = "NPS"
  }

  case object Ntc extends TaxType {
    override val pcipalProductionClientId: String = "NTPL"
    override val pcipalTestClientId: String = "NTPD"
    override val screenValue: String = "NTC"
  }

  case object Paye extends TaxType {
    override val pcipalProductionClientId: String = "PAPL"
    override val pcipalTestClientId: String = "PAPD"
    override val screenValue: String = "PAYE"
  }

  case object PNGR extends TaxType {
    override val pcipalProductionClientId: String = "PSML"
    override val pcipalTestClientId: String = "PPCE"
    override val screenValue: String = "PNGR"
  }

  //TODO: remove this tax type
  case object P800 extends TaxType {
    override def pcipalProductionClientId: String = throw new RuntimeException("p800 is not suported")
    override def pcipalTestClientId: String = throw new RuntimeException("p800 is not suported")
    override def screenValue: String = throw new RuntimeException("p800 is not suported")
  }

  case object Ppt extends TaxType {
    override val pcipalProductionClientId: String = genericPcipalProductionClientId
    override val pcipalTestClientId: String = genericPcipalTestClientId
    override val screenValue: String = "PPT"
  }

  case object Sa extends TaxType {
    override val pcipalProductionClientId: String = "SAPM"
    override val pcipalTestClientId: String = "SAPD"
    override val screenValue: String = "SA"
  }

  case object Safe extends TaxType {
    override val pcipalProductionClientId: String = "SFPL"
    override val pcipalTestClientId: String = "SFPD"
    override val screenValue: String = "SAFE"
  }

  case object Sdlt extends TaxType {
    override val pcipalProductionClientId: String = "SDPL"
    override val pcipalTestClientId: String = "SDPD"
    override val screenValue: String = "SDLT"
  }

  case object Vat extends TaxType {
    override val pcipalProductionClientId: String = "VAPM"
    override val pcipalTestClientId: String = "VAPD"
    override val screenValue: String = "VAT"
  }

  val usedOnFrontend: Seq[TaxType] = Seq[TaxType](ChildBenefitsRepayments, Sa, Sdlt, Safe, Cotax, Ntc, Paye, Nps, Vat, Ppt)
}
