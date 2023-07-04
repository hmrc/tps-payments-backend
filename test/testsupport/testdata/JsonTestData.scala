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

import play.api.libs.json.{JsValue, Json}
import testsupport.testdata.TestData._

import testsupport.RichMatchers._
import tps.journey.model.Journey

object JsonTestData {

  private val createdString: String = "2040-01-20T11:56:46Z"

  private def jsonBuilder(tpsPayments: Journey, paymentSpecificData: String, taxType: String) =
    s"""{
          "_id": "${tpsPayments._id.value}",
          "pid": "pid",
          "created": "$createdString",
          "payments": [
            {
              "paymentSpecificData": $paymentSpecificData,
              "amount": 100.00,
              "chargeReference": "chargeReference",
              "headOfDutyIndicator": "B",
              "paymentItemId": "${tpsPayments.payments.headOption.value.paymentItemId.value}",
              "updated": "$createdString",
              "customerName": "customerName",
              "taxType": "$taxType"
            }
          ],
          "navigation": {
              "back" : "back",
              "reset" : "reset",
              "finish" : "finish",
              "callback" : "callback"
            }
        }""".stripMargin

  val mibPaymentsJson: JsValue = Json.parse(jsonBuilder(
    tpsPayments         = mibPayments,
    paymentSpecificData = """{"chargeReference": "chargeReference","vat": 1,"customs": 2}""",
    taxType             = "MIB"
  ))

  val childBenefitsPaymentsJson: JsValue = Json.parse(jsonBuilder(
    tpsPayments         = childBenefitPayments,
    paymentSpecificData = """{"childBenefitYReference": "childBenefitRef"}""",
    taxType             = "ChildBenefitsRepayments"
  ))
  val saPaymentsJson: JsValue = Json.parse(jsonBuilder(
    tpsPayments         = saPayments,
    paymentSpecificData = """{"saReference": "saRef"}""",
    taxType             = "Sa"
  ))
  val sdltPaymentsJson: JsValue = Json.parse(jsonBuilder(
    tpsPayments         = sdltPayments,
    paymentSpecificData = """{"sdltReference": "sdltRef"}""",
    taxType             = "Sdlt"
  ))
  val safePaymentsJson: JsValue = Json.parse(jsonBuilder(
    tpsPayments         = safePayments,
    paymentSpecificData = """{"safeReference": "safeRef"}""",
    taxType             = "Safe"
  ))
  val cotaxPaymentsJson: JsValue = Json.parse(jsonBuilder(
    tpsPayments         = cotaxPayments,
    paymentSpecificData = """{"cotaxReference": "cotaxRef"}""",
    taxType             = "Cotax"
  ))
  val ntcPaymentsJson: JsValue = Json.parse(jsonBuilder(
    tpsPayments         = ntcPayments,
    paymentSpecificData = """{"ntcReference": "ntcRef"}""",
    taxType             = "Ntc"
  ))
  val payePaymentsJson: JsValue = Json.parse(jsonBuilder(
    tpsPayments         = payePayments,
    paymentSpecificData =
      """{
        |"payeReference": "payeRef",
        |"taxAmount": 100,
        |"nicAmount": 100
        |}""".stripMargin,
    taxType             = "Paye"
  ))
  val npsPaymentsJson: JsValue = Json.parse(jsonBuilder(
    tpsPayments         = npsPayments,
    paymentSpecificData =
      """{
        |"npsReference": "npsRef",
        |"periodStartDate": "1",
        |"periodEndDate": "2",
        |"npsType": "3",
        |"rate": 10
        |}""".stripMargin,
    taxType             = "Nps"
  ))
  val vatPaymentsJson: JsValue = Json.parse(jsonBuilder(
    tpsPayments         = vatPayments,
    paymentSpecificData = """{"vatReference": "vatRef", "remittanceType":"someRemittanceType"}""",
    taxType             = "Vat"
  ))
  val pptPaymentsJson: JsValue = Json.parse(jsonBuilder(
    tpsPayments         = pptPayments,
    paymentSpecificData = """{"pptReference": "pptRef"}""",
    taxType             = "Ppt"
  ))

}
