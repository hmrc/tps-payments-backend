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

package support.testdata

import deniedrefs.model.{DeniedRefs, DeniedRefsId, VerifyRefsRequest}
import model.StatusTypes.validated
import model.TaxTypes.{MIB, P800, PNGR, Sa}
import model._
import model.pcipal._
import paymentsprocessor.ModsPaymentCallBackRequest
import play.api.libs.json.{JsValue, Json}

import java.time.{Instant, LocalDateTime}

object TestData {
  private val createdString: String = "2040-01-20T11:56:46Z"
  private val createdStringLegacy: String = "2040-01-20T11:56:46"
  private val created: Instant = Instant.parse(createdString)
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
        email               = Some("test@email.com")
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
        email               = None
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
        email               = None
      )
    ),
    navigation = Navigation("back", "reset", "finish", "callback")
  )

  def tpsPaymentRequestGeneric(taxRegimeDisplay: String, taxType: TaxType, paymentSpecificData: PaymentSpecificData): TpsPaymentRequest = TpsPaymentRequest(
    pid        = "pid",
    payments   = Seq[TpsPaymentRequestItem](
      TpsPaymentRequestItem(
        chargeReference     = "chargeReference",
        customerName        = "customerName",
        amount              = BigDecimal("100.00"),
        taxRegimeDisplay    = taxRegimeDisplay,
        taxType             = taxType,
        paymentSpecificData = paymentSpecificData,
        email               = None
      )
    ),
    navigation = Navigation("back", "reset", "finish", "callback")
  )

  val mibPayments: TpsPayments = tpsPaymentRequestGeneric("MIB", TaxTypes.MIB, MibSpecificData("chargeReference", BigDecimal(1), BigDecimal(2))).tpsPayments(created)
  val childBenefitPayments: TpsPayments = tpsPaymentRequestGeneric("ChildBenefitsRepayments", TaxTypes.ChildBenefitsRepayments, ChildBenefitSpecificData("childBenefitRef")).tpsPayments(created)
  val saPayments: TpsPayments = tpsPaymentRequestGeneric("SA", TaxTypes.Sa, SaSpecificData("saRef")).tpsPayments(created)
  val sdltPayments: TpsPayments = tpsPaymentRequestGeneric("SDLT", TaxTypes.Sdlt, SdltSpecificData("sdltRef")).tpsPayments(created)
  val safePayments: TpsPayments = tpsPaymentRequestGeneric("SAFE", TaxTypes.Safe, SafeSpecificData("safeRef")).tpsPayments(created)
  val cotaxPayments: TpsPayments = tpsPaymentRequestGeneric("COTAX", TaxTypes.Cotax, CotaxSpecificData("cotaxRef")).tpsPayments(created)
  val ntcPayments: TpsPayments = tpsPaymentRequestGeneric("NTC", TaxTypes.Ntc, NtcSpecificData("ntcRef")).tpsPayments(created)
  val payePayments: TpsPayments = tpsPaymentRequestGeneric("PAYE", TaxTypes.Paye, PayeSpecificData("payeRef", BigDecimal(100), BigDecimal(100))).tpsPayments(created)
  val npsPayments: TpsPayments = tpsPaymentRequestGeneric("NPS", TaxTypes.Nps, NpsSpecificData("npsRef", "1", "2", "3", BigDecimal(10))).tpsPayments(created)
  val vatPayments: TpsPayments = tpsPaymentRequestGeneric("VAT", TaxTypes.Vat, VatSpecificData("vatRef", "someRemittanceType")).tpsPayments(created)
  val pptPayments: TpsPayments = tpsPaymentRequestGeneric("PPT", TaxTypes.Ppt, PptSpecificData("pptRef")).tpsPayments(created)

  val id: TpsId = TpsId("session-48c978bb-64b6-4a00-a1f1-51e267d84f91")
  val pciPalSessionId: PcipalSessionId = PcipalSessionId("48c978bb")
  val paymentItemId: PaymentItemId = PaymentItemId("paymentItemId-48c978bb-64b6-4a00-a1f1-51e267d84f91")

  val modsRef: String = "XMIB12345678"
  val modsLookupChargeRefs: List[String] = List(modsRef)
  val modsVatAmount = 123
  val modsCustomsAmount = 123

  val chargeRefNotificationPcipalRequest: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
    HoD                  = HeadOfDutyIndicators.B,
    TaxReference         = reference,
    Amount               = 1.92,
    Commission           = 1.23,
    CardType             = "VISA",
    Status               = validated,
    PCIPalSessionId      = pciPalSessionId,
    TransactionReference = transReference,
    paymentItemId        = paymentItemId,
    ChargeReference      = "chargeReference",
    ReferenceNumber      = "3000000001",
    CardLast4            = "0123"
  )

  val tpsPayments: TpsPayments =
    TpsPayments(
      id,
      pid,
      //      Some(pciPalSessionId),
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
          Some("test@email.com"))))

  val navigation = Navigation(back     = "back", reset = "reset", finish = "finish", callback = "callback")

  val pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
    FlowId              = 123,
    InitialValues       = List(
      PcipalInitialValues(
        clientId           = "clientId123",
        pid                = "pid007",
        accountOfficeId    = "accofficid001",
        HODIdentifier      = HeadOfDutyIndicators.K,
        UTRReference       = "1234567895K",
        name1              = "AR",
        amount             = "1.92",
        taxAmount          = None,
        nicAmount          = None,
        lnpClass2          = None,
        nirRate            = None,
        startDate          = None,
        endDate            = None,
        vatPeriodReference = None,
        vatRemittanceType  = None,
        paymentItemId      = PaymentItemId("payment-item-id123123"),
        chargeReference    = "1234567895K",
        taxRegimeDisplay   = "SA",
        reference          = "1234567895K",
        increment          = "1"
      )
    ),
    UTRBlacklistFlag    = "",
    postcodeFlag        = "",
    taxRegime           = "SA",
    TotalTaxAmountToPay = "1.92",
    callbackUrl         = navigation.callback,
    backUrl             = navigation.back,
    resetUrl            = navigation.reset,
    finishUrl           = navigation.finish
  )

  val tpsPaymentsWithPcipalData: TpsPayments =
    TpsPayments(
      _id = id,
      pid = pid,
      //      Some(pciPalSessionId),
      created                     = created,
      payments                    = List(
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
          Some("test@email.com"))
      ),
      navigation                  = Some(navigation),
      pcipalSessionLaunchRequest  = Some(pcipalSessionLaunchRequest),
      pcipalSessionLaunchResponse = Some(PcipalSessionLaunchResponse(Id = pciPalSessionId, "LinkId123"))
    )

  val tpsPaymentsWithEncryptedEmail: TpsPayments =
    TpsPayments(
      id,
      pid,
      //      Some(pciPalSessionId),
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
          Some("BEru9SQBlqfw0JgiAEKzUXm3zcq6eZHxYFdtl6Pw696S2y+d2gONPeX3MUFcLA=="))))

  val tpsPaymentsWithoutEmail: TpsPayments =
    TpsPayments(
      id,
      pid,
      //      Some(pciPalSessionId),
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
          None)))

  val tpsPaymentsWithEmptyEmail: TpsPayments =
    TpsPayments(
      id,
      pid,
      //      Some(pciPalSessionId),
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
          Some(""))))

  val modsTpsPaymentsNoAmendmentReference: TpsPayments = TpsPayments(
    _id = id,
    pid = pid,
    //    pciPalSessionId = Some(pciPalSessionId),
    created  = created,
    payments = List(
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
        email               = None)))

  val modsTpsPaymentsWithAnAmendmentReference: TpsPayments = TpsPayments(
    _id = id,
    pid = pid,
    //    pciPalSessionId = Some(pciPalSessionId),
    created  = created,
    payments = List(
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
        email               = None)))

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
            "ChargeReference" : "chargeReference",
            "ReferenceNumber": "3000000001",
            "CardLast4": "0123"
      }""".stripMargin)

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
            "email": "test@email.com"
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
          "created":  "$createdString",
          "payments": [
            {
              "paymentItemId" : "${paymentItemId.value}",
              "amount": 1.92,
              "headOfDutyIndicator": "B",
              "updated": "$createdString",
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
              "email": "test@email.com"
            }
          ]
        }
     """.stripMargin)

  //language=JSON
  val tpsPaymentsMongoJson: JsValue = Json.parse(
    s"""{
          "_id" : "${id.value}",
          "pid" : "$pid",
          "created": {
            "$$date": {
              "$$numberLong": "2210673406000"
            }
          },
          "payments": [
            {
              "paymentItemId" : "${paymentItemId.value}",
              "amount": 1.92,
              "headOfDutyIndicator": "B",
              "updated": "$createdString",
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
              "email": "test@email.com"
            }
          ]
        }
     """.stripMargin)

  //language=JSON
  val tpsPaymentsMongoLegacyJson: JsValue = Json.parse(
    s"""{
          "_id" : "${id.value}",
          "pid" : "$pid",
          "pciPalSessionId" : "${pciPalSessionId.value}",
          "created": "$createdStringLegacy",
          "payments": [
            {
              "paymentItemId" : "${paymentItemId.value}",
              "amount": 1.92,
              "headOfDutyIndicator": "B",
              "updated": "$createdString",
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
              "email": "test@email.com"
            }
          ]
        }
     """.stripMargin)

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

  val ref1 = Reference("REF1")
  val ref2 = Reference("REF2")
  val ref3 = Reference("REF3")
  val ref4 = Reference("REF4")
  val ref5 = Reference("REF5")

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
