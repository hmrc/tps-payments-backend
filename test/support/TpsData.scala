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

package support

import java.time.LocalDateTime

import model.StatusTypes.validated
import model.TaxType._
import model._
import model.pcipal.{ChargeRefNotificationPcipalRequest, PcipalSessionId}
import play.api.libs.json.{JsValue, Json}

object TpsData {
  private val created: LocalDateTime = LocalDateTime.parse("2020-01-20T11:56:46")
  private val reference = "JE231111"
  private val reference2 = "B"
  private val reference3 = "P800"
  private val pid = "123"
  private val transReference = "51e267d84f91"

  val tpsPaymentRequest: TpsPaymentRequest = TpsPaymentRequest(
    pid        = "pid",
    payments   = Seq[TpsPaymentRequestItem](
      TpsPaymentRequestItem(
        chargeReference     = "chargeReference",
        customerName        = "customerName",
        amount              = BigDecimal("100.00"),
        taxRegimeDisplay    = "taxRegimeDisplay",
        taxType             = MODS,
        paymentSpecificData = SimplePaymentSpecificData("chargeReference")
      )
    ),
    navigation = Navigation("back", "reset", "finish", "callback")
  )

  val tpsPaymentRequestPngr: TpsPaymentRequest = TpsPaymentRequest(
    pid        = "pid",
    payments   = Seq[TpsPaymentRequestItem](
      TpsPaymentRequestItem(
        chargeReference     = "chargeReference",
        customerName        = "customerName",
        amount              = BigDecimal("100.00"),
        taxRegimeDisplay    = "PNGR",
        taxType             = PNGR,
        paymentSpecificData = PngrSpecificData("chargeReference", BigDecimal("1.00"), BigDecimal("2.00"), BigDecimal("3.00"))
      )
    ),
    navigation = Navigation("back", "reset", "finish", "callback")
  )

  val tpsPaymentRequestMods: TpsPaymentRequest = TpsPaymentRequest(
    pid        = "pid",
    payments   = Seq[TpsPaymentRequestItem](
      TpsPaymentRequestItem(
        chargeReference     = "chargeReference",
        customerName        = "customerName",
        amount              = BigDecimal("100.00"),
        taxRegimeDisplay    = "MODS",
        taxType             = MODS,
        paymentSpecificData = ModsSpecificData("chargeReference", BigDecimal("1.00"), BigDecimal("2.00"))
      )
    ),
    navigation = Navigation("back", "reset", "finish", "callback")
  )

  val modsPayments: TpsPayments = tpsPaymentRequest.tpsPayments(created)

  val id: TpsId = TpsId("session-48c978bb-64b6-4a00-a1f1-51e267d84f91")
  val pciPalSessionId: PcipalSessionId = PcipalSessionId("48c978bb")
  val paymentItemId: PaymentItemId = PaymentItemId("paymentItemId-48c978bb-64b6-4a00-a1f1-51e267d84f91")

  val tpsPayments: TpsPayments =
    TpsPayments(
      id,
      pid,
      Some(pciPalSessionId),
      created,
      List(
        TpsPaymentItem(
          Some(paymentItemId),
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
    paymentItemId
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
            "paymentItemId": "${paymentItemId.value}",
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
              "paymentItemId" : "${paymentItemId.value}",
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
            "taxType": "MODS",
            "paymentSpecificData":{
              "chargeReference":"chargeReference"
            }
          }
          ],
          "navigation": {
              "back" : "back",
              "reset" : "reset",
              "finish" : "finish",
              "callback" : "callback"
            }
      } """.stripMargin)

  val paymentRequestPngrJson: JsValue = Json.parse(
    """{
        "pid": "pid",
        "payments": [
          {
            "chargeReference": "chargeReference",
            "customerName": "customerName",
            "amount": 100,
            "taxRegimeDisplay": "PNGR",
            "taxType": "PNGR",
            "paymentSpecificData":{
              "chargeReference":"chargeReference",
              "vat": 1,
              "customs": 2,
              "excise": 3
            }
          }
          ],
          "navigation": {
              "back" : "back",
              "reset" : "reset",
              "finish" : "finish",
              "callback" : "callback"
            }
      } """.stripMargin)

  val paymentRequestModsJson: JsValue = Json.parse(
    """{
        "pid": "pid",
        "payments": [
          {
            "chargeReference": "chargeReference",
            "customerName": "customerName",
            "amount": 100,
            "taxRegimeDisplay": "MODS",
            "taxType": "MODS",
            "paymentSpecificData":{
              "chargeReference":"chargeReference",
              "vat": 1,
              "customs": 2
            }
          }
          ],
          "navigation": {
              "back" : "back",
              "reset" : "reset",
              "finish" : "finish",
              "callback" : "callback"
            }
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
              "paymentItemId" : "${paymentItemId.value}",
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
  val modsPaymentsJson: JsValue = Json.parse(
    s"""{
          "_id": "${modsPayments._id.value}",
          "pid": "pid",
          "created": "$created",
          "payments": [
            {
              "paymentSpecificData": {
                "chargeReference": "chargeReference"
              },
              "amount": 100,
              "chargeReference": "chargeReference",
              "headOfDutyIndicator": "B",
              "paymentItemId": "${modsPayments.payments.head.paymentItemId.get.value}",
              "updated": "$created",
              "customerName": "customerName",
              "taxType": "MODS"
            }
          ],
          "navigation": {
              "back" : "back",
              "reset" : "reset",
              "finish" : "finish",
              "callback" : "callback"
            }
        }""".stripMargin)
}
