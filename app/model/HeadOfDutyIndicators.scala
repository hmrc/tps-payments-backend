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

package model

import controllers.ValueClassBinder._
import enumeratum._
import enumformat.EnumFormat
import play.api.libs.json.Format
import play.api.mvc.{PathBindable, QueryStringBindable}

import scala.collection.immutable

object HeadOfDutyIndicator {
  implicit val format: Format[HeadOfDutyIndicator] = EnumFormat(HeadOfDutyIndicators)
  implicit val pathBinder: QueryStringBindable[HeadOfDutyIndicator] = bindableA(_.toString)
  implicit val headOfDutyBinder: PathBindable[HeadOfDutyIndicator] = valueClassBinder(_.toString)

}

sealed abstract class HeadOfDutyIndicator extends EnumEntry {
}

object HeadOfDutyIndicators extends Enum[HeadOfDutyIndicator] {

  def forCode(code: String): HeadOfDutyIndicator = values.find(_.toString == code) match {
    case Some(x) => x
    case None    => throw new RuntimeException(s"Could not find code $code")
  }

  def forCode(code: HeadOfDutyIndicator): Option[HeadOfDutyIndicator] = values.find(_ == code)

  /**
   * Hod for P800 and Child Benefits
   */
  case object B extends HeadOfDutyIndicator

  /**
   * Hod for COTAX
   */
  case object A extends HeadOfDutyIndicator

  /**
   * Hod for NTC
   */
  case object N extends HeadOfDutyIndicator

  /**
   * Hod for SDLT
   */
  case object M extends HeadOfDutyIndicator

  /**
   * Hod for NPS
   */
  case object J extends HeadOfDutyIndicator

  /**
   * Hod for PAYE
   */
  case object P extends HeadOfDutyIndicator

  /**
   * Hod for VAT
   */
  case object V extends HeadOfDutyIndicator

  /**
   * Hod for Safe
   */
  case object X extends HeadOfDutyIndicator

  /**
   * Hod for Self Assesment (Cesa)
   */
  case object K extends HeadOfDutyIndicator

  /**
   * Hod for Ppt
   */
  case object C extends HeadOfDutyIndicator

  def values: immutable.IndexedSeq[HeadOfDutyIndicator] = findValues

}
