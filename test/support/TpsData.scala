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
import reactivemongo.bson.BSONObjectID

object TpsData {

  val created = LocalDateTime.now
  val id = TpsId(BSONObjectID.generate.stringify)
  val tpsPayment: TpsPayment = TpsPayment(1.92, TaxTypes.cds, StatusTypes.SENT, "12345X", None, created)
  val tpsPayments: TpsPayments = TpsPayments(Some(id), created, List(tpsPayment))

  //language=JSON
  val tpsPaymentsJson: JsValue = Json.parse(
    s"""{
        "_id" : "${id.value}",
        "created":  "${created}",
       "payments": [
        {
        "amount": 1.92,
        "taxType": "cds",
        "status": "SENT",
        "reference": "12345X",
        "updated": "${created}"
        }
        ]
        }
     """.stripMargin
  )

}
