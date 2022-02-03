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

import play.api.libs.json.{Format, Json, OFormat}
import play.api.libs.functional.syntax._

final case class Utr(value: String)

object Utr {
  implicit val format: Format[Utr] = implicitly[Format[String]].inmap(Utr(_), _.value)

  def canonicalizeUtr(utr: Utr): Utr = {
    val trimmed = utr.value.trim
    val lowercased = trimmed.toUpperCase()
    val withoutK = dropK(lowercased)
    utr.copy(value = s"${withoutK}K")
  }

  private def dropK(utrString: String) = {
    if (utrString.toLowerCase().startsWith("k")) {
      utrString.drop(1)
    } else if (utrString.toLowerCase().endsWith("k")) {
      utrString.dropRight(1)
    } else utrString
  }
}
