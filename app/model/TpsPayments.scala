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

package model

import model.pcipal.{PcipalSessionId, PcipalSessionLaunchRequest, PcipalSessionLaunchResponse}
import play.api.libs.json.{Json, OFormat}
import repository.Repo.HasId

import java.time.Instant

final case class TpsPayments(
    _id:                         TpsId,
    pid:                         String,
    created:                     Instant                             = Instant.now(),
    payments:                    List[TpsPaymentItem],
    navigation:                  Option[Navigation]                  = None,
    pcipalSessionLaunchRequest:  Option[PcipalSessionLaunchRequest]  = None,
    pcipalSessionLaunchResponse: Option[PcipalSessionLaunchResponse] = None
) extends HasId[TpsId] {
  lazy val pciPalSessionId: Option[PcipalSessionId] = pcipalSessionLaunchResponse.map(_.Id)
}

object TpsPayments {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[TpsPayments] = Json.format[TpsPayments]
}
