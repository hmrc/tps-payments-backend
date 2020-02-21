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
import play.api.libs.json.{JsValue, Json}

object TpsData {

  val created = LocalDateTime.parse("2020-01-20T11:56:46")
  val id = TpsId("session-48c978bb-64b6-4a00-a1f1-51e267d84f91")
  val pid = "123"
  val tpsPayment: TpsPayment = TpsPayment(Some(id), 1.92, TaxTypes.cds, StatusTypes.sent, "12345X", None, created, Some(2000), "AR")
  val tpsPayments: TpsPayments = TpsPayments(Some(id), pid, created, List(tpsPayment))

  //language=JSON
  val tpsPaymentsJson: JsValue = Json.parse(
    s"""{
        "_id" : "${id.value}",
        "pid" : "${pid}",
        "created":  "${created}",
       "payments": [
        {
         "paymentId" : "${id.value}",
        "amount": 1.92,
        "taxType": "cds",
        "status": "sent",
        "reference": "12345X",
        "updated": "${created}",
         "period": 2000,
         "customerName" : "AR"
        }
        ]
        }
     """.stripMargin
  )

}
