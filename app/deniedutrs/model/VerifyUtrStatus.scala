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

package deniedutrs.model

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.Format
import util.EnumFormat

import scala.collection.immutable

sealed abstract class VerifyUtrStatus extends EnumEntry

object VerifyUtrStatuses extends Enum[VerifyUtrStatus] {

  case object UtrDenied extends VerifyUtrStatus
  case object UtrPermitted extends VerifyUtrStatus
  case object MissingInformation extends VerifyUtrStatus

  override def values: immutable.IndexedSeq[VerifyUtrStatus] = findValues
}

object VerifyUtrStatus {
  implicit val format: Format[VerifyUtrStatus] = Format(
    EnumFormat(VerifyUtrStatuses),
    EnumFormat(VerifyUtrStatuses)
  )
}