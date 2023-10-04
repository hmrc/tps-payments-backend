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

package testsupport.testdata

import paymentsprocessor.ModsPaymentCallBackRequest
import play.api.libs.json.{JsValue, Json}
import tps.journey.model.{Journey, JourneyId, JourneyState}
import tps.model.TaxTypes.{MIB, PNGR}
import tps.model._
import tps.pcipalmodel.StatusTypes.validated
import tps.pcipalmodel._
import tps.startjourneymodel
import tps.startjourneymodel.{SjPaymentItem, StartJourneyRequestMibOrPngr}

import java.time.Instant

object TestData {
  val navigation = Navigation(back     = "back", reset = "reset", finish = "finish", callback = "callback")

  private val createdString: String = "2040-01-20T11:56:46Z"
  private val createdStringLegacy: String = "2040-01-20T11:56:46"

  private val created: Instant = Instant.parse(createdString)
  private val reference = "JE231111"
  private val reference2 = "B"
  private val reference3 = "P800"
  private val pid = "123"
  private val transReference = "51e267d84f91"

  val tpsPaymentRequest: StartJourneyRequestMibOrPngr = startjourneymodel.StartJourneyRequestMibOrPngr(
    pid        = "pid",
    payments   = Seq[SjPaymentItem](
      SjPaymentItem(
        chargeReference     = "chargeReference",
        customerName        = CustomerName("customerName"),
        amount              = BigDecimal("100.00"),
        taxRegimeDisplay    = "taxRegimeDisplay",
        taxType             = PNGR,
        paymentSpecificData = PngrSpecificData("chargeReference"),
        email               = Some(Email("test@email.com"))
      )
    ),
    navigation = Navigation("back", "reset", "finish", "callback")
  )

  val tpsPaymentRequestPngr: StartJourneyRequestMibOrPngr = startjourneymodel.StartJourneyRequestMibOrPngr(
    pid        = "pid",
    payments   = Seq[SjPaymentItem](
      SjPaymentItem(
        chargeReference     = "chargeReference",
        customerName        = CustomerName("customerName"),
        amount              = BigDecimal("100.00"),
        taxRegimeDisplay    = "PNGR",
        taxType             = PNGR,
        paymentSpecificData = PngrSpecificData("chargeReference"),
        email               = None
      )
    ),
    navigation = Navigation("back", "reset", "finish", "callback")
  )

  val tpsPaymentRequestMib: StartJourneyRequestMibOrPngr = startjourneymodel.StartJourneyRequestMibOrPngr(
    pid        = "pid",
    payments   = Seq[SjPaymentItem](
      SjPaymentItem(
        chargeReference     = "chargeReference",
        customerName        = CustomerName("customerName"),
        amount              = BigDecimal("100.00"),
        taxRegimeDisplay    = "MIB",
        taxType             = MIB,
        paymentSpecificData = MibSpecificData("chargeReference", BigDecimal("1.00"), BigDecimal("2.00")),
        email               = None
      )
    ),
    navigation = Navigation("back", "reset", "finish", "callback")
  )

  def startJourneyRequestMibOrPngr(taxRegimeDisplay: String, taxType: TaxType, paymentSpecificData: PaymentSpecificData): StartJourneyRequestMibOrPngr = startjourneymodel.StartJourneyRequestMibOrPngr(
    pid        = "pid",
    payments   = Seq[SjPaymentItem](
      SjPaymentItem(
        chargeReference     = "chargeReference",
        customerName        = CustomerName("customerName"),
        amount              = BigDecimal("100.00"),
        taxRegimeDisplay    = taxRegimeDisplay,
        taxType             = taxType,
        paymentSpecificData = paymentSpecificData,
        email               = None
      )
    ),
    navigation = Navigation("back", "reset", "finish", "callback")
  )

  val mibPayments: Journey = startJourneyRequestMibOrPngr("MIB", TaxTypes.MIB, MibSpecificData("chargeReference", BigDecimal(1), BigDecimal(2))).makeJourney(created)
  val childBenefitPayments: Journey = startJourneyRequestMibOrPngr("ChildBenefitsRepayments", TaxTypes.ChildBenefitsRepayments, ChildBenefitSpecificData("childBenefitRef")).makeJourney(created)
  val saPayments: Journey = startJourneyRequestMibOrPngr("SA", TaxTypes.Sa, SaSpecificData("saRef")).makeJourney(created)
  val sdltPayments: Journey = startJourneyRequestMibOrPngr("SDLT", TaxTypes.Sdlt, SdltSpecificData("sdltRef")).makeJourney(created)
  val safePayments: Journey = startJourneyRequestMibOrPngr("SAFE", TaxTypes.Safe, SafeSpecificData("safeRef")).makeJourney(created)
  val cotaxPayments: Journey = startJourneyRequestMibOrPngr("COTAX", TaxTypes.Cotax, CotaxSpecificData("cotaxRef")).makeJourney(created)
  val ntcPayments: Journey = startJourneyRequestMibOrPngr("NTC", TaxTypes.Ntc, NtcSpecificData("ntcRef")).makeJourney(created)
  val payePayments: Journey = startJourneyRequestMibOrPngr("PAYE", TaxTypes.Paye, PayeSpecificData("payeRef", BigDecimal(100), BigDecimal(100))).makeJourney(created)
  val npsPayments: Journey = startJourneyRequestMibOrPngr("NPS", TaxTypes.Nps, NpsSpecificData("npsRef", "1", "2", "3", BigDecimal(10))).makeJourney(created)
  val vatPayments: Journey = startJourneyRequestMibOrPngr("VAT", TaxTypes.Vat, VatSpecificData("vatRef", "someRemittanceType")).makeJourney(created)
  val pptPayments: Journey = startJourneyRequestMibOrPngr("PPT", TaxTypes.Ppt, PptSpecificData("pptRef")).makeJourney(created)

  val id: JourneyId = JourneyId("session-48c978bb-64b6-4a00-a1f1-51e267d84f91")
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
    ReferenceNumber      = "3123456701",
    CardLast4            = "0123"
  )

  val journey: Journey =
    Journey(
      id,
      journeyState = JourneyState.Started,
      pid,
      //      Some(pciPalSessionId),
      created,
      List(
        PaymentItem(
          paymentItemId,
          1.92,
          HeadOfDutyIndicators.B,
          created,
          CustomerName("some test name"),
          "12345",
          None,
          ChildBenefitSpecificData(reference),
          TaxTypes.ChildBenefitsRepayments,
          Some(Email("test@email.com")))
      ),
      navigation                  = navigation,
      pcipalSessionLaunchRequest  = None,
      pcipalSessionLaunchResponse = None
    )

  val pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
    FlowId              = 123,
    InitialValues       = List(
      PcipalInitialValues(
        clientId           = "clientId123",
        pid                = "pid007",
        accountOfficeId    = "accofficid001",
        HODIdentifier      = HeadOfDutyIndicators.K,
        UTRReference       = "1234567895K",
        name1              = "some test name",
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
        reference          = "300001",
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

  val tpsPaymentsWithPcipalData: Journey =
    Journey(
      _id          = id,
      journeyState = JourneyState.Started,
      pid          = pid,
      //      Some(pciPalSessionId),
      created                     = created,
      payments                    = List(
        PaymentItem(
          paymentItemId,
          1.92,
          HeadOfDutyIndicators.B,
          created,
          CustomerName("some test name"),
          "12345",
          Some(chargeRefNotificationPcipalRequest),
          ChildBenefitSpecificData(reference),
          TaxTypes.ChildBenefitsRepayments,
          Some(Email("test@email.com")))
      ),
      navigation                  = navigation,
      pcipalSessionLaunchRequest  = Some(pcipalSessionLaunchRequest),
      pcipalSessionLaunchResponse = Some(PcipalSessionLaunchResponse(Id = pciPalSessionId, "LinkId123"))
    )

  val tpsPaymentsWithEncryptedEmail: Journey =
    Journey(
      id,
      journeyState = JourneyState.Started,
      pid,
      //      Some(pciPalSessionId),
      created,
      List(
        PaymentItem(
          paymentItemId,
          1.92,
          HeadOfDutyIndicators.B,
          created,
          CustomerName("some test name"),
          "12345",
          None,
          ChildBenefitSpecificData(reference),
          TaxTypes.ChildBenefitsRepayments,
          Some(Email("BEru9SQBlqfw0JgiAEKzUXm3zcq6eZHxYFdtl6Pw696S2y+d2gONPeX3MUFcLA==")))
      ),
      navigation                  = navigation,
      pcipalSessionLaunchRequest  = None,
      pcipalSessionLaunchResponse = None
    )

  val tpsPaymentsWithoutEmail: Journey =
    Journey(
      id,
      journeyState = JourneyState.Started,
      pid,
      //      Some(pciPalSessionId),
      created,
      List(
        PaymentItem(
          paymentItemId,
          1.92,
          HeadOfDutyIndicators.B,
          created,
          CustomerName("some test name"),
          "12345",
          None,
          ChildBenefitSpecificData(reference),
          TaxTypes.ChildBenefitsRepayments,
          None)),
      navigation                  = navigation,
      pcipalSessionLaunchRequest  = None,
      pcipalSessionLaunchResponse = None
    )

  val tpsPaymentsWithEmptyEmail: Journey =
    Journey(
      _id          = id,
      journeyState = JourneyState.Started,
      pid,
      //      Some(pciPalSessionId),
      created,
      List(
        PaymentItem(
          paymentItemId,
          1.92,
          HeadOfDutyIndicators.B,
          created,
          CustomerName("some test name"),
          "12345",
          None,
          ChildBenefitSpecificData(reference),
          TaxTypes.ChildBenefitsRepayments,
          Some(Email.emptyEmail))
      ),
      navigation                  = navigation,
      pcipalSessionLaunchRequest  = None,
      pcipalSessionLaunchResponse = None
    )

  val modsTpsPaymentsNoAmendmentReference: Journey = Journey(
    _id                         = id,
    journeyState                = JourneyState.Started,
    pid                         = pid,
    created                     = created,
    payments                    = List(
      PaymentItem(
        paymentItemId       = paymentItemId,
        amount              = 1.92,
        headOfDutyIndicator = HeadOfDutyIndicators.B,
        updated             = created,
        customerName        = CustomerName("Bob Ross"),
        chargeReference     = modsRef,
        pcipalData          = None,
        paymentSpecificData = MibSpecificData(
          chargeReference    = modsRef,
          vat                = modsVatAmount,
          customs            = modsCustomsAmount,
          amendmentReference = None
        ),
        taxType             = MIB,
        email               = None)),
    navigation                  = navigation,
    pcipalSessionLaunchRequest  = None,
    pcipalSessionLaunchResponse = None
  )

  val modsTpsPaymentsWithAnAmendmentReference: Journey = Journey(
    _id                         = id,
    journeyState                = JourneyState.Started,
    pid                         = pid,
    created                     = created,
    payments                    = List(
      PaymentItem(
        paymentItemId       = paymentItemId,
        amount              = 1.92,
        headOfDutyIndicator = HeadOfDutyIndicators.B,
        updated             = created,
        customerName        = CustomerName("Bob Ross"),
        chargeReference     = modsRef,
        pcipalData          = None,
        paymentSpecificData = MibSpecificData(
          chargeReference    = modsRef,
          vat                = modsVatAmount,
          customs            = modsCustomsAmount,
          amendmentReference = Some(1)
        ),
        taxType             = MIB,
        email               = None)
    ),
    navigation                  = navigation,
    pcipalSessionLaunchRequest  = None,
    pcipalSessionLaunchResponse = None
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
            "ChargeReference" : "chargeReference",
            "ReferenceNumber": "3123456701",
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
            "taxType": "PNGR",
            "paymentSpecificData":{"chargeReference":"chargeReference","vat":22,"customs":15,"excise":5},
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
          "journeyState" : "Landing",
          "pid" : "$pid",
          "created":  "$createdString",
          "payments": [
            {
              "paymentItemId" : "${paymentItemId.value}",
              "amount": 1.92,
              "headOfDutyIndicator": "B",
              "updated": "$createdString",
              "customerName" : "some test name",
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
          ],
          "navigation": {
            "back" : "back",
            "reset" : "reset",
            "finish" : "finish",
            "callback" : "callback"
          }
        }
     """.stripMargin)

  //language=JSON
  val journeyMongoJson: JsValue = Json.parse(
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
              "customerName" : "some test name",
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
          ],
          "navigation": {
            "back" : "back",
            "reset" : "reset",
            "finish" : "finish",
            "callback" : "callback"
          }
        }
     """.stripMargin)

  //language=JSON
  val tpsPaymentsMongoLegacyJson: JsValue = Json.parse(
    s"""{
          "_id" : "${id.value}",
          "journeyState" : {
            "Landing" : { }
          },
          "pid" : "$pid",
          "pciPalSessionId" : "${pciPalSessionId.value}",
          "created": "$createdStringLegacy",
          "payments": [
            {
              "paymentItemId" : "${paymentItemId.value}",
              "amount": 1.92,
              "headOfDutyIndicator": "B",
              "updated": "$createdString",
              "customerName" : "some test name",
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

  val tpsItemsForEmail: String = """[{"taxType":"Child Benefits repayments","amount":"1.92","transactionFee":"1.23","transactionNumber":"3123456701"}]"""

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

}
