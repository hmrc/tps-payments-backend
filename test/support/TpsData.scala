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

package support

import java.time.LocalDateTime

import model._
import model.pcipal.{ChargeRefNotificationPcipalRequest, PcipalSessionId}
import play.api.libs.json.{JsValue, Json}

object TpsData {

  val created = LocalDateTime.parse("2020-01-20T11:56:46")
  val id = TpsId("session-48c978bb-64b6-4a00-a1f1-51e267d84f91")
  val paymentId = PaymentItemId("session-48c978bb-64b6-4a00-a1f1-51e267d84f91")
  val pciPalSessionId = PcipalSessionId("48c978bb")
  val reference = "JE231111B"
  val pid = "123"
  val transReference = "51e267d84f91"
  val tpsPayment: TpsPaymentItem = TpsPaymentItem(Some(paymentId), 1.92, TaxTypes.CDSX, reference, created, Some(2000), "AR", None)
  val tpsPayments: TpsPayments = TpsPayments(id, pid, Some(pciPalSessionId), created, List(tpsPayment))

  val chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
    TaxTypes.CDSX,
    reference,
    1.92,
    1.23,
    "VISA",
    StatusTypes.complete,
    pciPalSessionId,
    transReference,
    paymentId
  )

  //language=JSON
  val chargeRefNotificationPciPalRequestJson = Json.parse(
    s"""{
            "HoD": "CDSX",
            "TaxReference": "${reference}",
            "Amount": 1.92,
            "Commission": 1.23,
            "CardType": "VISA",
            "Status": "${StatusTypes.complete.toString}",
            "PCIPalSessionId": "${pciPalSessionId.value}",
            "TransactionReference": "${transReference}",
            "paymentItemId": "${paymentId.value}"
      }""".stripMargin)

  //language=JSON
  val tpsPaymentsJson: JsValue = Json.parse(
    s"""{
        "_id" : "${id.value}",
        "pid" : "${pid}",
        "pciPalSessionId" : "${pciPalSessionId.value}",
        "created":  "${created}",
       "payments": [
        {
         "paymentItemId" : "${paymentId.value}",
        "amount": 1.92,
        "taxType": "CDSX",
        "reference": "${reference}",
        "updated": "${created}",
         "period": 2000,
         "customerName" : "AR"
        }
        ]
        }
     """.stripMargin
  )

}
