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
import play.api.libs.json._

sealed trait PaymentSpecificData {
  def getReference: String
}

final case class PaymentSpecificDataP800(
    ninoPart1:          String,
    ninoPart2:          String,
    taxTypeScreenValue: String,
    period:             Int
) extends PaymentSpecificData {
  def getReference: String = {
    s"$ninoPart1$ninoPart2$taxTypeScreenValue$period"
  }
}

object PaymentSpecificDataP800 {
  implicit val format: OFormat[PaymentSpecificDataP800] = Json.format[PaymentSpecificDataP800]
}

final case class SimplePaymentSpecificData(chargeReference: String) extends PaymentSpecificData {
  override def getReference: String = chargeReference
}

object SimplePaymentSpecificData {
  implicit val format: OFormat[SimplePaymentSpecificData] = Json.format[SimplePaymentSpecificData]
}

final case class PngrSpecificData(chargeReference: String,
                                  vat:             BigDecimal,
                                  customs:         BigDecimal,
                                  excise:          BigDecimal) extends PaymentSpecificData {
  override def getReference: String = chargeReference
}

object PngrSpecificData {
  implicit val format: OFormat[PngrSpecificData] = Json.format[PngrSpecificData]
}

final case class MibSpecificData(chargeReference:    String,
                                 vat:                BigDecimal,
                                 customs:            BigDecimal,
                                 amendmentReference: Option[Int] = None
) extends PaymentSpecificData {
  override def getReference: String = chargeReference
  def getAmendmentReference: Option[Int] = amendmentReference
}

object MibSpecificData {
  implicit val format: OFormat[MibSpecificData] = Json.format[MibSpecificData]
}

final case class ChildBenefitSpecificData(
    childBenefitYReference: String
) extends PaymentSpecificData {
  override def getReference: String = childBenefitYReference
}
object ChildBenefitSpecificData {
  implicit val format: OFormat[ChildBenefitSpecificData] = Json.format[ChildBenefitSpecificData]
}

final case class SaSpecificData(
    saReference: String
) extends PaymentSpecificData {
  override def getReference: String = saReference
}
object SaSpecificData {
  implicit val format: OFormat[SaSpecificData] = Json.format[SaSpecificData]
}

final case class SdltSpecificData(
    sdltReference: String
) extends PaymentSpecificData {
  override def getReference: String = sdltReference
}
object SdltSpecificData {
  implicit val format: OFormat[SdltSpecificData] = Json.format[SdltSpecificData]
}

final case class SafeSpecificData(
    safeReference: String
) extends PaymentSpecificData {
  override def getReference: String = safeReference
}
object SafeSpecificData {
  implicit val format: OFormat[SafeSpecificData] = Json.format[SafeSpecificData]
}

object PaymentSpecificData {
  implicit val writes: Writes[PaymentSpecificData] = Writes[PaymentSpecificData] {
    case p800: PaymentSpecificDataP800                      => PaymentSpecificDataP800.format.writes(p800)
    case simple: SimplePaymentSpecificData                  => SimplePaymentSpecificData.format.writes(simple)
    case pngr: PngrSpecificData                             => PngrSpecificData.format.writes(pngr)
    case mib: MibSpecificData                               => MibSpecificData.format.writes(mib)
    case childBenefitSpecificData: ChildBenefitSpecificData => ChildBenefitSpecificData.format.writes(childBenefitSpecificData)
    case sa: SaSpecificData                                 => SaSpecificData.format.writes(sa)
    case sdltSpecificData: SdltSpecificData                 => SdltSpecificData.format.writes(sdltSpecificData)
    case safeSpecificData: SafeSpecificData                 => SafeSpecificData.format.writes(safeSpecificData)
  }

  implicit val reads: Reads[PaymentSpecificData] = Reads[PaymentSpecificData] {
    case json: JsObject if json.keys == jsonKeysSimplePaymentSpecificData =>
      JsSuccess(json.as[SimplePaymentSpecificData])
    case json: JsObject if json.keys == jsonKeysPaymentSpecificDataP800 =>
      JsSuccess(json.as[PaymentSpecificDataP800])
    case json: JsObject if json.keys == jsonKeysPngrSpecificData =>
      JsSuccess(json.as[PngrSpecificData])
    case json: JsObject if (json.keys == jsonKeysMibSpecificDataVariant1) || (json.keys == jsonKeysMibSpecificDataVariant2) =>
      JsSuccess(json.as[MibSpecificData])
    case json: JsObject if json.keys == jsonKeysChildBenefit =>
      JsSuccess(json.as[ChildBenefitSpecificData])
    case json: JsObject if json.keys == jsonKeysSa =>
      JsSuccess(json.as[SaSpecificData])
    case json: JsObject if json.keys == jsonKeysSdlt =>
       JsSuccess(json.as[SdltSpecificData])
    case json: JsObject if json.keys == jsonKeysSafe =>
      JsSuccess(json.as[SafeSpecificData])
  }

  val jsonKeysSimplePaymentSpecificData = Set("chargeReference")
  val jsonKeysPaymentSpecificDataP800 = Set("ninoPart1", "ninoPart2", "taxTypeScreenValue", "period")
  val jsonKeysPngrSpecificData = Set("chargeReference", "vat", "customs", "excise")
  val jsonKeysMibSpecificDataVariant1 = Set("chargeReference", "vat", "customs")
  val jsonKeysMibSpecificDataVariant2 = Set("chargeReference", "vat", "customs", "amendmentReference")
  val jsonKeysChildBenefit = Set("childBenefitYReference")
  val jsonKeysSa = Set("saReference")
  val jsonKeysSdlt = Set("sdltReference")
  val jsonKeysSafe = Set("safeReference")
}
