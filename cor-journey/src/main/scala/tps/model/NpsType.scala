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

package tps.model

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.Format

import scala.collection.immutable

sealed trait NpsType extends EnumEntry {
  val pcipalValue: String
  val screenValue: String
}

object NpsType {
  implicit val format: Format[NpsType] = tps.utils.EnumFormat(NpsTypes)
}

object NpsTypes extends Enum[NpsType] {
  val values: immutable.IndexedSeq[NpsType] = findValues

  case object Class2NationalInsurance extends NpsType {
    override val pcipalValue: String = "61"
    override val screenValue: String = "Class 2 National Insurance"
  }

  case object LateNotificationPenalty extends NpsType {
    override val pcipalValue: String = "11"
    override val screenValue: String = "Late notification penalty"
  }

}
