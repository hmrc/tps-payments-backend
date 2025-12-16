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

import play.api.libs.json._
import tps.pcipalmodel.ChargeRefNotificationPcipalRequest

import java.time.Instant

final case class PaymentItem(
  paymentItemId:       PaymentItemId,
  amount:              BigDecimal,
  headOfDutyIndicator: HeadOfDutyIndicator,
  updated:             Instant,
  customerName:        CustomerName,
  chargeReference:     String,
  // this is updated upon receiving notification from PciPal via payments-processor
  pcipalData:          Option[ChargeRefNotificationPcipalRequest] = None,
  paymentSpecificData: PaymentSpecificData,
  taxType:             TaxType, // TODO: remove it and derive it as val from HoD (or vice versa)
  email:               Option[Email]
) {

  def getPcipalData: ChargeRefNotificationPcipalRequest =
    pcipalData.getOrElse(throw new RuntimeException(s"Expected PciPal data to be there [${paymentItemId.toString}]"))
}

object PaymentItem {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def formats: OFormat[PaymentItem] = Json.format[PaymentItem]
}
