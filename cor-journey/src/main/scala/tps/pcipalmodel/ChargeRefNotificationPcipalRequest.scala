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

package tps.pcipalmodel

import play.api.libs.json.{Json, OFormat}
import tps.model.{HeadOfDutyIndicator, PaymentItemId}

/**
 * This represents notification data being sent from PciPal to the payments-processor.
 * Also named as PciPal data in other places
 */
final case class ChargeRefNotificationPcipalRequest(
    HoD:                  HeadOfDutyIndicator,
    TaxReference:         String,
    Amount:               BigDecimal,
    Commission:           BigDecimal,
    CardType:             String,
    Status:               StatusType,
    PCIPalSessionId:      PcipalSessionId,
    TransactionReference: String,
    paymentItemId:        PaymentItemId,
    ChargeReference:      String              = "",
    ReferenceNumber:      String,
    CardLast4:            String
)

object ChargeRefNotificationPcipalRequest {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[ChargeRefNotificationPcipalRequest] = Json.format[ChargeRefNotificationPcipalRequest]
}
