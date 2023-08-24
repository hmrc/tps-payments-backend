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
import cats.Eq

sealed trait TaxType extends EnumEntry {
  def pcipalProductionClientId: String
  def pcipalTestClientId: String
  def screenValue: String
  def hod: HeadOfDutyIndicator

  def clientId(usePcipalTestSettings: Boolean): String =
    if (usePcipalTestSettings) pcipalTestClientId else pcipalProductionClientId
}

object TaxType {
  implicit val format: Format[TaxType] = tps.utils.EnumFormat(TaxTypes)
  implicit val pathBinder: QueryStringBindable[TaxType] = tps.utils.ValueClassBinder.bindableA(_.toString)
  implicit val taxTypeBinder: PathBindable[TaxType] = tps.utils.ValueClassBinder.valueClassBinder(_.toString)
  implicit val eq: Eq[TaxType] = Eq.fromUniversalEquals
}

/**
 * If you are adding a new tax regime, please make it all capitals, length 1-4.
 * This value is used by /payment-items/:paymentItemId/tax-type in payments processor and there is a requirement in the des spec for length to be 1-4.
 */
object TaxTypes extends Enum[TaxType] {
  private val genericPcipalProductionClientId = "TPSETMP"
  private val genericPcipalTestClientId = "TPSP800"
  val values: immutable.IndexedSeq[TaxType] = findValues

  /**
   * Child benefit repayment
   */
  case object ZCHB extends TaxType {
    override val hod: HeadOfDutyIndicator = HeadOfDutyIndicators.B
    override val pcipalProductionClientId: String = "CBCE"
    override val pcipalTestClientId: String = "CBCE"
    override val screenValue: String = "Repay Child Benefit overpayments"
  }

  /**
   * Corporation tax
   */
  case object CT extends TaxType {
    override val hod: HeadOfDutyIndicator = HeadOfDutyIndicators.A
    override val pcipalProductionClientId: String = "COPL"
    override val pcipalTestClientId: String = "COPD"
    override val screenValue: String = "COTAX"
  }

  /**
   * Merchandise in baggage/mods
   */
  case object MIB extends TaxType {
    override def hod: HeadOfDutyIndicator = throw new UnsupportedOperationException(s"MIB should not be looking for its HoD")
    override val pcipalProductionClientId: String = "MBML"
    override val pcipalTestClientId: String = "MPCE"
    override val screenValue: String = "MIB"
  }

  /**
   * NPS/NIRS
   */
  case object NPS extends TaxType {
    override val hod: HeadOfDutyIndicator = HeadOfDutyIndicators.J
    override val pcipalProductionClientId: String = "NPPL"
    override val pcipalTestClientId: String = "NPPD"
    override val screenValue: String = "NPS"
  }

  /**
   * Tax credit repayments
   */
  case object NTC extends TaxType {
    override val hod: HeadOfDutyIndicator = HeadOfDutyIndicators.N
    override val pcipalProductionClientId: String = "NTPL"
    override val pcipalTestClientId: String = "NTPD"
    override val screenValue: String = "NTC"
  }

  /**
   * Pay as you earn
   */
  case object PAYE extends TaxType {
    override val hod: HeadOfDutyIndicator = HeadOfDutyIndicators.P
    override val pcipalProductionClientId: String = "PAPL"
    override val pcipalTestClientId: String = "PAPD"
    override val screenValue: String = "PAYE"
  }

  /**
   * Passengers
   */
  case object PNGR extends TaxType {
    override def hod: HeadOfDutyIndicator = throw new UnsupportedOperationException(s"PNGR should not be looking for its HoD")
    override val pcipalProductionClientId: String = "PSML"
    override val pcipalTestClientId: String = "PPCE"
    override val screenValue: String = "PNGR"
  }

  //TODO: remove this tax type
  case object P800 extends TaxType {
    override def hod: HeadOfDutyIndicator = throw new UnsupportedOperationException(s"P800 should not be looking for its HoD")
    override def pcipalProductionClientId: String = throw new RuntimeException("p800 is not suported")
    override def pcipalTestClientId: String = throw new RuntimeException("p800 is not suported")
    override def screenValue: String = throw new RuntimeException("p800 is not suported")
  }

  /**
   * Plastic packaging tax
   */
  case object PPT extends TaxType {
    override val hod: HeadOfDutyIndicator = HeadOfDutyIndicators.C
    override val pcipalProductionClientId: String = genericPcipalProductionClientId
    override val pcipalTestClientId: String = genericPcipalTestClientId
    override val screenValue: String = "PPT"
  }

  /**
   * Self assessment
   */
  case object SA extends TaxType {
    override val hod: HeadOfDutyIndicator = HeadOfDutyIndicators.K
    override val pcipalProductionClientId: String = "SAPM"
    override val pcipalTestClientId: String = "SAPD"
    override val screenValue: String = "SA"
  }

  /**
   * Safe
   */
  case object SAFE extends TaxType {
    override val hod: HeadOfDutyIndicator = HeadOfDutyIndicators.X
    override val pcipalProductionClientId: String = "SFPL"
    override val pcipalTestClientId: String = "SFPD"
    override val screenValue: String = "SAFE"
  }

  /**
   * Stamp duty land tax
   */
  case object SDLT extends TaxType {
    override val hod: HeadOfDutyIndicator = HeadOfDutyIndicators.M
    override val pcipalProductionClientId: String = "SDPL"
    override val pcipalTestClientId: String = "SDPD"
    override val screenValue: String = "SDLT"
  }

  /**
   * Value added tax
   */
  case object VAT extends TaxType {
    override val hod: HeadOfDutyIndicator = HeadOfDutyIndicators.V
    override val pcipalProductionClientId: String = "VAPM"
    override val pcipalTestClientId: String = "VAPD"
    override val screenValue: String = "VAT"
  }

  val usedOnFrontend: Seq[TaxType] = Seq[TaxType](ZCHB, SA, SDLT, SAFE, CT, NTC, PAYE, NPS, VAT, PPT)
}
