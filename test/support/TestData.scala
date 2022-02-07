/*
 * Copyright 2022 HM Revenue & Customs
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

import deniedutrs.model._

import java.time.LocalDateTime
import model.StatusTypes.validated
import model.TaxTypes._
import model._
import model.pcipal.{ChargeRefNotificationPcipalRequest, PcipalSessionId}
import paymentsprocessor.ModsPaymentCallBackRequest
import play.api.libs.json.{JsValue, Json}

object TestData {
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
        taxType             = Sa,
        paymentSpecificData = SimplePaymentSpecificData("chargeReference"),
        email               = Some("test@email.com"),
        languageCode        = Some("en")
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
        paymentSpecificData = PngrSpecificData("chargeReference", BigDecimal("1.00"), BigDecimal("2.00"), BigDecimal("3.00")),
        email               = "email"
      )
    ),
    navigation = Navigation("back", "reset", "finish", "callback")
  )

  val tpsPaymentRequestMib: TpsPaymentRequest = TpsPaymentRequest(
    pid        = "pid",
    payments   = Seq[TpsPaymentRequestItem](
      TpsPaymentRequestItem(
        chargeReference     = "chargeReference",
        customerName        = "customerName",
        amount              = BigDecimal("100.00"),
        taxRegimeDisplay    = "MIB",
        taxType             = MIB,
        paymentSpecificData = MibSpecificData("chargeReference", BigDecimal("1.00"), BigDecimal("2.00")),
        email               = None,
        languageCode        = None
      )
    ),
    navigation = Navigation("back", "reset", "finish", "callback")
  )

  val mibPayments: TpsPayments = tpsPaymentRequestMib.tpsPayments(created)
  
  val id: TpsId = TpsId("session-48c978bb-64b6-4a00-a1f1-51e267d84f91")
  val pciPalSessionId: PcipalSessionId = PcipalSessionId("48c978bb")
  val paymentItemId: PaymentItemId = PaymentItemId("paymentItemId-48c978bb-64b6-4a00-a1f1-51e267d84f91")

  val modsRef: String = "XMIB12345678"
  val modsLookupChargeRefs: List[String] = List(modsRef)
  val modsVatAmount = 123
  val modsCustomsAmount = 123

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
          P800,
          Some("test@email.com"),
          Some("en"))))

  val modsTpsPaymentsNoAmendmentReference: TpsPayments = TpsPayments(
    _id             = id,
    pid             = pid,
    pciPalSessionId = Some(pciPalSessionId),
    created         = created,
    payments        = List(
      TpsPaymentItem(
        paymentItemId       = Some(paymentItemId),
        amount              = 1.92,
        headOfDutyIndicator = HeadOfDutyIndicators.B,
        updated             = created,
        customerName        = "Bob Ross",
        chargeReference     = modsRef,
        pcipalData          = None,
        paymentSpecificData = MibSpecificData(
          chargeReference    = modsRef,
          vat                = modsVatAmount,
          customs            = modsCustomsAmount,
          amendmentReference = None
        ),
        taxType             = MIB,
        email               = None,
        languageCode        = None)))

  val modsTpsPaymentsWithAnAmendmentReference: TpsPayments = TpsPayments(
    _id             = id,
    pid             = pid,
    pciPalSessionId = Some(pciPalSessionId),
    created         = created,
    payments        = List(
      TpsPaymentItem(
        paymentItemId       = Some(paymentItemId),
        amount              = 1.92,
        headOfDutyIndicator = HeadOfDutyIndicators.B,
        updated             = created,
        customerName        = "Bob Ross",
        chargeReference     = modsRef,
        pcipalData          = None,
        paymentSpecificData = MibSpecificData(
          chargeReference    = modsRef,
          vat                = modsVatAmount,
          customs            = modsCustomsAmount,
          amendmentReference = Some(1)
        ),
        taxType             = MIB,
        email               = None,
        languageCode        = None)))

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
              },
              "email": "test@email.com",
              "languageCode": "en"
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
            "taxType": "Sa",
            "paymentSpecificData":{
              "chargeReference":"chargeReference"
            },
            "email": "test@email.com",
            "languageCode": "en"
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

  val paymentRequestMibJson: JsValue = Json.parse(
    """{
        "pid": "pid",
        "payments": [
          {
            "chargeReference": "chargeReference",
            "customerName": "customerName",
            "amount": 100,
            "taxRegimeDisplay": "MIB",
            "taxType": "MIB",
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
            "taxType": "UNKNOWN",
            "email": "email"
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
              },
              "email": "test@email.com",
              "languageCode": "en"
            }
          ]
        }
     """.stripMargin)

  //language=JSON
  val mibPaymentsJson: JsValue = Json.parse(
    s"""{
          "_id": "${mibPayments._id.value}",
          "pid": "pid",
          "created": "$created",
          "payments": [
            {
              "paymentSpecificData": {
                "chargeReference": "chargeReference",
                "vat": 1,
                "customs": 2
              },
              "amount": 100,
              "chargeReference": "chargeReference",
              "headOfDutyIndicator": "B",
              "paymentItemId": "${mibPayments.payments.head.paymentItemId.get.value}",
              "updated": "$created",
              "customerName": "customerName",
              "taxType": "MIB"
            }
          ],
          "navigation": {
              "back" : "back",
              "reset" : "reset",
              "finish" : "finish",
              "callback" : "callback"
            }
        }""".stripMargin)

  //language=JSON
  val modsReconLookupJson: JsValue = Json.parse(
    s"""
       [
           {
               "chargeReference": "$modsRef",
               "vat": 123,
               "customs": 123
           }
       ]""".stripMargin
  )

  val modsReconLookup: List[MibSpecificData] = List(MibSpecificData(chargeReference = modsRef, vat = modsVatAmount, customs = modsCustomsAmount))

  val modsPaymentCallBackRequestWithAmendmentRef: ModsPaymentCallBackRequest = ModsPaymentCallBackRequest("XMIB12345678", Some(1))
  val modsPaymentCallBackRequestWithoutAmendmentRef: ModsPaymentCallBackRequest = ModsPaymentCallBackRequest("XMIB12345678", None)

  val utr1 = Utr("utr1")
  val utr2 = Utr("utr2")
  val utr3 = Utr("utr3")
  val utr4 = Utr("utr4")
  val utr5 = Utr("utr5")

  val csvFile1: String =
    s"""${utr1.value}
        |${utr2.value}
        |${utr3.value}
        |""".stripMargin

  val csvFile2: String =
    s"""${utr2.value}
        |${utr3.value}
        |${utr4.value}
        |${utr5.value}
        |""".stripMargin

  val deniedUtrs1 = DeniedUtrs(
    _id      = DeniedUtrsId("denied-utrs-id-123"),
    utrs     = List(utr1, utr2, utr3),
    inserted = LocalDateTime.parse("2022-02-04T10:00:24.371")
  )

  val deniedUtrs2 = DeniedUtrs(
    _id      = DeniedUtrsId("denied-utrs-id-123"),
    utrs     = List(utr2, utr3, utr4),
    inserted = LocalDateTime.parse("2022-02-05T10:00:24.371")
  )

  val verifyUtrRequest = VerifyUtrsRequest(Set(utr1))

}
