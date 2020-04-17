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

import model.pcipal.ChargeRefNotificationPcipalRequest
import play.api.libs.json.{Json, OFormat}

case class TpsPaymentItem(
    paymentItemId:       Option[PaymentItemId]                      = None,
    amount:              BigDecimal,
    headOfDutyIndicator: HeadOfDutyIndicator,
    referencePart1:      String,
    referencePart2:      Option[String],
    referencePart3:      Option[String],
    updated:             LocalDateTime                              = LocalDateTime.now(),
    period:              Option[Int],
    customerName:        String,
    chargeReference:     String                                     = "",
    pcipalData:          Option[ChargeRefNotificationPcipalRequest] = None) {

  def getReference: String = {
    referencePart1 + referencePart2.getOrElse("") + referencePart3.getOrElse("")
  }
}

object TpsPaymentItem {
  implicit val format: OFormat[TpsPaymentItem] = Json.format[TpsPaymentItem]

}
