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

package model
import play.api.libs.json._

sealed trait PaymentSpecificData {
  def getReference: String
}

final case class PaymentSpecificDataP800(
    ninoPart1:          String,
    ninoPart2:          String,
    taxTypeScreenValue: String,
    period:             Int
) extends PaymentSpecificData {
  def getReference: String = {
    s"$ninoPart1$ninoPart2$taxTypeScreenValue$period"
  }
}

object PaymentSpecificDataP800 {
  implicit val format: OFormat[PaymentSpecificDataP800] = Json.format[PaymentSpecificDataP800]

}

object PaymentSpecificData {
  implicit val writes: Writes[PaymentSpecificData] = Writes[PaymentSpecificData] {
    case psd: PaymentSpecificDataP800 => PaymentSpecificDataP800.format.writes(psd)
  }

}
