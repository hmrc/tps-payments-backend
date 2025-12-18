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

import play.api.libs.json.{Json, OFormat}

import play.api.libs.json._

sealed trait PaymentSpecificData {
  def getReference: String
  def getRawReference: String
}

final case class PngrSpecificData(
  chargeReference: String
) extends PaymentSpecificData {
  override def getReference: String    = chargeReference
  override def getRawReference: String = chargeReference
}

object PngrSpecificData {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[PngrSpecificData] = Json.format[PngrSpecificData]
}

final case class MibSpecificData(
  chargeReference:    String,
  vat:                BigDecimal,
  customs:            BigDecimal,
  amendmentReference: Option[Int] = None
) extends PaymentSpecificData {
  override def getReference: String      = chargeReference
  def getAmendmentReference: Option[Int] = amendmentReference
  override def getRawReference: String   = chargeReference
}

object MibSpecificData          {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[MibSpecificData] = Json.format[MibSpecificData]
}

final case class ChildBenefitSpecificData(
  childBenefitYReference: String
) extends PaymentSpecificData {
  override def getReference: String    = childBenefitYReference
  override def getRawReference: String = childBenefitYReference
}
object ChildBenefitSpecificData {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[ChildBenefitSpecificData] = Json.format[ChildBenefitSpecificData]
}

final case class SaSpecificData(
  saReference: String // TODO make strong type for that UTR
) extends PaymentSpecificData {
  override def getReference: String    = saReference
  override def getRawReference: String = saReference.dropRight(1)
}

object SaSpecificData    {
  implicit val format: OFormat[SaSpecificData] = Json.format[SaSpecificData]
}

final case class SdltSpecificData(
  sdltReference: String
) extends PaymentSpecificData {
  override def getReference: String    = sdltReference
  override def getRawReference: String = sdltReference
}
object SdltSpecificData  {
  implicit val format: OFormat[SdltSpecificData] = Json.format[SdltSpecificData]
}

final case class SafeSpecificData(
  safeReference: String
) extends PaymentSpecificData {
  override def getReference: String    = safeReference
  override def getRawReference: String = safeReference
}
object SafeSpecificData  {
  implicit val format: OFormat[SafeSpecificData] = Json.format[SafeSpecificData]
}

final case class CotaxSpecificData(
  cotaxReference: String
) extends PaymentSpecificData {
  override def getReference: String    = cotaxReference
  override def getRawReference: String = cotaxReference.dropRight(7)
}
object CotaxSpecificData {
  implicit val format: OFormat[CotaxSpecificData] = Json.format[CotaxSpecificData]
}

final case class NtcSpecificData(
  ntcReference: String
) extends PaymentSpecificData {
  override def getReference: String    = ntcReference
  override def getRawReference: String = ntcReference.dropRight(8)
}
object NtcSpecificData   {
  implicit val format: OFormat[NtcSpecificData] = Json.format[NtcSpecificData]
}

final case class PayeSpecificData(
  payeReference: String,
  taxAmount:     BigDecimal,
  nicAmount:     BigDecimal
) extends PaymentSpecificData {
  override def getReference: String    = payeReference
  override def getRawReference: String = payeReference.dropRight(4)
}
object PayeSpecificData  {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[PayeSpecificData] = Json.format[PayeSpecificData]
}

final case class NpsSpecificData(
  npsReference:    String,
  periodStartDate: String,
  periodEndDate:   String,
  npsType:         String,
  rate:            BigDecimal
) extends PaymentSpecificData {
  override def getReference: String    = npsReference
  override def getRawReference: String = npsReference.dropRight(2)
}
object NpsSpecificData   {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[NpsSpecificData] = Json.format[NpsSpecificData]
}

final case class VatSpecificData(
  vatReference:   String,
  remittanceType: String // TODO make strong type, enum
) extends PaymentSpecificData {
  override def getReference: String    = vatReference
  override def getRawReference: String = vatReference.dropRight(4)
}

object VatSpecificData {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[VatSpecificData] = Json.format[VatSpecificData]
}

final case class PptSpecificData(
  pptReference: String
) extends PaymentSpecificData {
  override def getReference: String    = pptReference
  override def getRawReference: String = pptReference
}
object PptSpecificData {
  implicit val format: OFormat[PptSpecificData] = Json.format[PptSpecificData]
}

object PaymentSpecificData {
  implicit val writes: Writes[PaymentSpecificData] = Writes[PaymentSpecificData] {
    case pngr: PngrSpecificData                             => PngrSpecificData.format.writes(pngr)
    case mib: MibSpecificData                               => MibSpecificData.format.writes(mib)
    case childBenefitSpecificData: ChildBenefitSpecificData =>
      ChildBenefitSpecificData.format.writes(childBenefitSpecificData)
    case sa: SaSpecificData                                 => SaSpecificData.format.writes(sa)
    case sdltSpecificData: SdltSpecificData                 => SdltSpecificData.format.writes(sdltSpecificData)
    case safeSpecificData: SafeSpecificData                 => SafeSpecificData.format.writes(safeSpecificData)
    case cotaxSpecificData: CotaxSpecificData               => CotaxSpecificData.format.writes(cotaxSpecificData)
    case ntcSpecificData: NtcSpecificData                   => NtcSpecificData.format.writes(ntcSpecificData)
    case payeSpecificData: PayeSpecificData                 => PayeSpecificData.format.writes(payeSpecificData)
    case npsSpecificData: NpsSpecificData                   => NpsSpecificData.format.writes(npsSpecificData)
    case vatSpecificData: VatSpecificData                   => VatSpecificData.format.writes(vatSpecificData)
    case pptSpecificData: PptSpecificData                   => PptSpecificData.format.writes(pptSpecificData)
  }

  implicit val reads: Reads[PaymentSpecificData] = Reads[PaymentSpecificData] {
    case json: JsObject if json.keys == jsonKeysPngrSpecificData       =>
      JsSuccess(json.as[PngrSpecificData])
    case json: JsObject if json.keys == jsonKeysPngrSpecificDataLegacy =>
      JsSuccess(json.as[PngrSpecificData])
    case json: JsObject
        if (json.keys == jsonKeysMibSpecificDataVariant1) || (json.keys == jsonKeysMibSpecificDataVariant2) =>
      JsSuccess(json.as[MibSpecificData])
    case json: JsObject if json.keys == jsonKeysChildBenefit           =>
      JsSuccess(json.as[ChildBenefitSpecificData])
    case json: JsObject if json.keys == jsonKeysSa                     =>
      JsSuccess(json.as[SaSpecificData])
    case json: JsObject if json.keys == jsonKeysSdlt                   =>
      JsSuccess(json.as[SdltSpecificData])
    case json: JsObject if json.keys == jsonKeysSafe                   =>
      JsSuccess(json.as[SafeSpecificData])
    case json: JsObject if json.keys == jsonKeysCotax                  =>
      JsSuccess(json.as[CotaxSpecificData])
    case json: JsObject if json.keys == jsonKeysNtc                    =>
      JsSuccess(json.as[NtcSpecificData])
    case json: JsObject
        if (json.keys == jsonKeysPayeVariant1) || (json.keys == jsonKeysPayeVariant2) || (json.keys == jsonKeysPayeVariant3) =>
      JsSuccess(json.as[PayeSpecificData])
    case json: JsObject if json.keys == jsonKeysNps                    =>
      JsSuccess(json.as[NpsSpecificData])
    case json: JsObject if json.keys == jsonKeysVat                    =>
      JsSuccess(json.as[VatSpecificData])
    case json: JsObject if json.keys == jsonKeysPpt                    =>
      JsSuccess(json.as[PptSpecificData])
    case _                                                             =>
      JsError("Could not read PaymentSpecificData")
  }

  val jsonKeysPngrSpecificData: Set[String]        = Set("chargeReference", "vat", "customs", "excise")
  val jsonKeysPngrSpecificDataLegacy: Set[String]  = Set("chargeReference")
  val jsonKeysMibSpecificDataVariant1: Set[String] = Set("chargeReference", "vat", "customs")
  val jsonKeysMibSpecificDataVariant2: Set[String] = Set("chargeReference", "vat", "customs", "amendmentReference")
  val jsonKeysChildBenefit: Set[String]            = Set("childBenefitYReference")
  val jsonKeysSa: Set[String]                      = Set("saReference")
  val jsonKeysSdlt: Set[String]                    = Set("sdltReference")
  val jsonKeysSafe: Set[String]                    = Set("safeReference")
  val jsonKeysCotax: Set[String]                   = Set("cotaxReference")
  val jsonKeysNtc: Set[String]                     = Set("ntcReference")
  val jsonKeysPayeVariant1: Set[String]            = Set("payeReference", "taxAmount")
  val jsonKeysPayeVariant2: Set[String]            = Set("payeReference", "nicAmount")
  val jsonKeysPayeVariant3: Set[String]            = Set("payeReference", "taxAmount", "nicAmount")
  val jsonKeysNps: Set[String]                     = Set("npsReference", "periodStartDate", "periodEndDate", "npsType", "rate")
  val jsonKeysVat: Set[String]                     = Set("vatReference", "remittanceType")
  val jsonKeysPpt: Set[String]                     = Set("pptReference")
}
