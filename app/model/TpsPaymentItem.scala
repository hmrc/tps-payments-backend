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

import model.pcipal.ChargeRefNotificationPcipalRequest
import play.api.libs.json._
import tps.model.{HeadOfDutyIndicator, PaymentItemId}

import java.time.Instant

final case class TpsPaymentItem(
    paymentItemId:       Option[PaymentItemId],
    amount:              BigDecimal,
    headOfDutyIndicator: HeadOfDutyIndicator,
    updated:             Instant,
    customerName:        String,
    chargeReference:     String                = "",
    //this is updated upon receiving notification from PciPal via payments-processor
    pcipalData:          Option[ChargeRefNotificationPcipalRequest] = None,
    paymentSpecificData: PaymentSpecificData,
    taxType:             TaxType,
    email:               Option[String]) {
}

object TpsPaymentItem {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def formats: OFormat[TpsPaymentItem] = Json.format[TpsPaymentItem]
}
