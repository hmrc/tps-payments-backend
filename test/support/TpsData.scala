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

import model.StatusTypes.validated
import model.TaxType._
import model._
import model.pcipal.{ChargeRefNotificationPcipalRequest, PcipalSessionId}
import play.api.libs.json.{JsValue, Json}

object TpsData {
  private val created: LocalDateTime = LocalDateTime.parse("2020-01-20T11:56:46")
  private val paymentId: PaymentItemId = PaymentItemId("session-48c978bb-64b6-4a00-a1f1-51e267d84f91")
  private val reference = "JE231111"
  private val reference2 = "B"
  private val reference3 = "P800"
  private val pid = "123"
  private val transReference = "51e267d84f91"

  val tpsPaymentRequest: TpsPaymentRequest = TpsPaymentRequest(
    pid      = "pid",
    payments = Seq[TpsPaymentRequestItem](
      TpsPaymentRequestItem(
        chargeReference  = "chargeReference",
        customerName     = "customerName",
        amount           = BigDecimal("100.00"),
        taxRegimeDisplay = "taxRegimeDisplay",
        taxType          = MIB
      )
    )
  )

  val mibPayments: TpsPayments = tpsPaymentRequest.tpsPayments
  val mibCreated: String = mibPayments.created.toString

  val id: TpsId = TpsId("session-48c978bb-64b6-4a00-a1f1-51e267d84f91")
  val pciPalSessionId: PcipalSessionId = PcipalSessionId("48c978bb")

  val tpsPayments: TpsPayments =
    TpsPayments(
      id,
      pid,
      Some(pciPalSessionId),
      created,
      List(
        TpsPaymentItem(
          Some(paymentId),
          1.92,
          HeadOfDutyIndicators.B,
          created,
          "AR",
          "",
          None,
          PaymentSpecificDataP800(reference, reference2, reference3, 2000),
          P800)))

  val chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
    HeadOfDutyIndicators.B,
    reference,
    1.92,
    1.23,
    "VISA",
    validated,
    pciPalSessionId,
    transReference,
    paymentId
  )

  //language=JSON
  val chargeRefNotificationPciPalRequestJson: JsValue = Json.parse(
    s"""{
            "HoD": "B",
            "TaxReference": "$reference",
            "Amount": 1.92,
            "Commission": 1.23,
            "CardType": "VISA",
            "Status": "${validated.toString}",
            "PCIPalSessionId": "${pciPalSessionId.value}",
            "TransactionReference": "$transReference",
            "paymentItemId": "${paymentId.value}",
            "ChargeReference" : ""
      }""".stripMargin)

  //language=JSON
  val tpsPaymentsJsonWithoutTaxType: JsValue = Json.parse(
    s"""{
          "_id" : "${id.value}",
          "pid" : "$pid",
          "pciPalSessionId" : "${pciPalSessionId.value}",
          "created":  "$created",
          "payments": [
            {
              "paymentItemId" : "${paymentId.value}",
              "amount": 1.92,
              "headOfDutyIndicator": "B",
              "updated": "$created",
              "customerName" : "AR",
              "chargeReference" : "",
              "paymentSpecificData" :
              {
                "ninoPart1": "$reference",
                "ninoPart2": "$reference2",
                "taxTypeScreenValue": "$reference3",
                "period": 2000
              }
            }
          ]
        }
     """.stripMargin)

  val paymentRequestJson: JsValue = Json.parse(
    """{
        "pid": "pid",
        "payments": [
          {
            "chargeReference": "chargeReference",
            "customerName": "customerName",
            "amount": 100,
            "taxRegimeDisplay": "taxRegimeDisplay",
            "taxType": "MIB"
          }
        ]
      } """.stripMargin)

  val invalidPaymentRequestJson: JsValue = Json.parse(
    """{
        "pid": "pid",
        "payments": [
          {
            "chargeReference": "chargeReference",
            "customerName": "customerName",
            "amount": 100,
            "taxRegimeDisplay": "taxRegimeDisplay",
            "taxType": "UNKNOWN"
          }
        ]
      } """.stripMargin)

  //language=JSON
  val tpsPaymentsJson: JsValue = Json.parse(
    s"""{
          "_id" : "${id.value}",
          "pid" : "$pid",
          "pciPalSessionId" : "${pciPalSessionId.value}",
          "created":  "$created",
          "payments": [
            {
              "paymentItemId" : "${paymentId.value}",
              "amount": 1.92,
              "headOfDutyIndicator": "B",
              "updated": "$created",
              "customerName" : "AR",
              "taxType": "P800",
              "chargeReference" : "",
              "paymentSpecificData" :
              {
                "ninoPart1": "$reference",
                "ninoPart2": "$reference2",
                "taxTypeScreenValue": "$reference3",
                "period": 2000
              }
            }
          ]
        }
     """.stripMargin)

  //language=JSON
  val mibPaymentsJson: JsValue = Json.parse(
    s"""{
          "_id": "${mibPayments._id.value}",
          "pid": "pid",
          "created": "$mibCreated",
          "payments": [
            {
              "paymentSpecificData": {
                "chargeReference": "chargeReference"
              },
              "amount": 100,
              "chargeReference": "chargeReference",
              "headOfDutyIndicator": "B",
              "paymentItemId": "${mibPayments.payments.head.paymentItemId.get.value}",
              "updated": "$mibCreated",
              "customerName": "customerName",
              "taxType": "MIB"
            }
          ]
        }""".stripMargin)
}
