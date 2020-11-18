/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDateTime

import enumeratum._
import model.TaxType.P800
import model.pcipal.ChargeRefNotificationPcipalRequest
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.collection.immutable

sealed trait TaxType extends EnumEntry {
}

object TaxType extends PlayEnum[TaxType] {
  val values: immutable.IndexedSeq[TaxType] = findValues

  case object P800 extends TaxType
  case object MIB extends TaxType
  case object PNGR extends TaxType
}

case class TpsPaymentItem(
    paymentItemId:       Option[PaymentItemId],
    amount:              BigDecimal,
    headOfDutyIndicator: HeadOfDutyIndicator,
    updated:             LocalDateTime                              = LocalDateTime.now(),
    customerName:        String,
    chargeReference:     String                                     = "",
    pcipalData:          Option[ChargeRefNotificationPcipalRequest] = None,
    paymentSpecificData: PaymentSpecificData,
    taxType:             TaxType) {
}

object TpsPaymentItem {

  implicit val writes: OWrites[TpsPaymentItem] = {
    OWrites[TpsPaymentItem](tpsPaymentItem =>
      Json.obj(
        s"amount" -> tpsPaymentItem.amount,
        s"headOfDutyIndicator" -> tpsPaymentItem.headOfDutyIndicator,
        s"updated" -> tpsPaymentItem.updated,
        s"customerName" -> tpsPaymentItem.customerName,
        s"chargeReference" -> tpsPaymentItem.chargeReference,
        s"paymentSpecificData" -> tpsPaymentItem.paymentSpecificData,
        s"taxType" -> tpsPaymentItem.taxType.toString
      )
        ++ tpsPaymentItem.pcipalData.map(pd => Json.obj("pcipalData" -> pd)).getOrElse(Json.obj())
        ++ tpsPaymentItem.paymentItemId.map(pid => Json.obj("paymentItemId" -> pid)).getOrElse(Json.obj())
    )
  }

  private val reads: Reads[TpsPaymentItem] =
    (
      (__ \ "paymentItemId").readNullable[PaymentItemId] and
      (__ \ "amount").read[BigDecimal] and
      (__ \ "headOfDutyIndicator").read[HeadOfDutyIndicator] and
      (__ \ "updated").read[LocalDateTime] and
      (__ \ "customerName").read[String] and
      (__ \ "chargeReference").read[String] and
      (__ \ "pcipalData").readNullable[ChargeRefNotificationPcipalRequest] and
      (__ \ "paymentSpecificData").read[PaymentSpecificData] and
      (__ \ "taxType").readWithDefault[String](P800.toString)
    ) ((pid, amnt, hod, updt, cn, cr, pd, psd, taxType) =>
        TpsPaymentItem(pid, amnt, hod, updt, cn, cr, pd, psd, TaxType.namesToValuesMap(taxType)))

  implicit def formats: OFormat[TpsPaymentItem] = OFormat(reads, writes)

}
