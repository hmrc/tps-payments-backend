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

import deniedrefs.model._

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
        email               = None,
        languageCode        = None
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
  
  val chargeRefNotificationPcipalRequest: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
    HoD                  = HeadOfDutyIndicators.B,
    TaxReference         = "sssss",
    Amount               = 10,
    Commission           = 1,
    CardType             = "Visa",
    Status               = StatusTypes.validated,
    PCIPalSessionId      = PcipalSessionId("aaaa"),
    TransactionReference = "transactionReference",
    PaymentItemId        = PaymentItemId("aaa"),
    ChargeReference      = "chargeReference",
    ReferenceNumber      = "3000000001",
    CardLast4            = "0123"
  )

  val chargeRefNotificationPcipalRequest: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
    HoD                  = HeadOfDutyIndicators.B,
    TaxReference         = reference,
    Amount               = 1.92,
    Commission           = 1.23,
    CardType             = "VISA",
    Status               = validated,
    PCIPalSessionId      = pciPalSessionId,
    TransactionReference = transReference,
    PaymentItemId        = paymentItemId,
    ChargeReference      = "chargeReference",
    ReferenceNumber      = "3000000001",
    CardLast4            = "0123"
  )

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
          "12345",
          None,
          PaymentSpecificDataP800(reference, reference2, reference3, 2000),
          P800,
          Some("test@email.com"),
          Some("en"))))
  
   val tpsPaymentsWithPcipalData: TpsPayments =
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
          "12345",
          Some(chargeRefNotificationPcipalRequest),
          PaymentSpecificDataP800(reference, reference2, reference3, 2000),
          P800,
          Some("test@email.com"),
          Some("en"))))

  val tpsPaymentsWithPcipalData: TpsPayments =
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
          "12345",
          Some(chargeRefNotificationPcipalRequest),
          PaymentSpecificDataP800(reference, reference2, reference3, 2000),
          P800,
          Some("test@email.com"),
          Some("en"))))

  val tpsPaymentsWithEncryptedEmail: TpsPayments =
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
          Some("uu5HocTKj0V0Uo2QD4JrHVXqIug3MQOJWL0KYq8kkIPMYLNc5wVefB7vkeRvCQ=="),
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
            "PaymentItemId": "${paymentItemId.value}",
            "ChargeReference" : "chargeReference",
            "ReferenceNumber": "3000000001",
            "CardLast4": "0123"
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
              "chargeReference" : "12345",
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
              "chargeReference" : "12345",
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

  val tpsItemsForEmail: String = """[{"taxType":"P800","amount":"1.92","transactionFee":"1.23","transactionNumber":"3000000001"}]"""

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

  val ref1 = Reference("ref1")
  val ref2 = Reference("ref2")
  val ref3 = Reference("ref3")
  val ref4 = Reference("ref4")
  val ref5 = Reference("ref5")

  val csvFile1: String =
    s"""${ref1.value}
        |${ref2.value}
        |${ref3.value}
        |""".stripMargin

  val csvFile2: String =
    s"""${ref2.value}
        |${ref3.value}
        |${ref4.value}
        |${ref5.value}
        |""".stripMargin

  val deniedRefs1 = DeniedRefs(
    _id      = DeniedRefsId("denied-refs-id-123"),
    refs     = List(ref1, ref2, ref3),
    inserted = LocalDateTime.parse("2022-02-04T10:00:24.371")
  )

  val deniedRefs2 = DeniedRefs(
    _id      = DeniedRefsId("denied-refs-id-123"),
    refs     = List(ref2, ref3, ref4),
    inserted = LocalDateTime.parse("2022-02-05T10:00:24.371")
  )

  val verifyRefRequest = VerifyRefsRequest(Set(ref1))

}
