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

package model

import java.time.{Instant, LocalDateTime, ZoneOffset}
import model.pcipal.ChargeRefNotificationPcipalRequest
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class TpsPaymentItem(
    paymentItemId:       Option[PaymentItemId],
    amount:              BigDecimal,
    headOfDutyIndicator: HeadOfDutyIndicator,
    updated:             Instant,
    customerName:        String,
    chargeReference:     String                                     = "",
    pcipalData:          Option[ChargeRefNotificationPcipalRequest] = None,
    paymentSpecificData: PaymentSpecificData,
    taxType:             TaxType,
    email:               Option[String]) {
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
        ++ tpsPaymentItem.email.map(email => Json.obj("email" -> email)).getOrElse(Json.obj())
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def formats: OFormat[TpsPaymentItem] = Json.format[TpsPaymentItem]
}
