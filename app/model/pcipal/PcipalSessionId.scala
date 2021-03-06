/*
 * Copyright 2021 HM Revenue & Customs
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

package model.pcipal

import controllers.ValueClassBinder.valueClassBinder
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.PathBindable

final case class PcipalSessionId(
    value: String
)

object PcipalSessionId {
  implicit val format: Format[PcipalSessionId] = implicitly[Format[String]].inmap(PcipalSessionId(_), _.value)
  implicit val pciPalIdBinder: PathBindable[PcipalSessionId] = valueClassBinder(_.value)
}

