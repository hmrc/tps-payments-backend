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

package tps.journey.model

import play.api.libs.json._
import tps.model.repo.HasId
import tps.model.{Navigation, PaymentItem, PaymentItemId}
import tps.pcipalmodel.{PcipalSessionId, PcipalSessionLaunchRequest, PcipalSessionLaunchResponse}
import tps.utils.SafeEquals.EqualsOps

import java.time.Instant

final case class Journey(
  _id:                         JourneyId,
  journeyState:                JourneyState,
  pid:                         String,
  created:                     Instant,
  payments:                    List[
    PaymentItem
  ], // note that field is in mongo query, refactor wisely making sure historical records are also updated
  navigation:                  Navigation,
  pcipalSessionLaunchRequest:  Option[PcipalSessionLaunchRequest] = None,
  pcipalSessionLaunchResponse: Option[PcipalSessionLaunchResponse] = None
) extends HasId[JourneyId] {
  def journeyId: JourneyId                          = _id
  lazy val pciPalSessionId: Option[PcipalSessionId] = pcipalSessionLaunchResponse.map(_.Id)
  def basketEmpty: Boolean                          = payments.size === 0
  def basketNonEmpty: Boolean                       = !basketEmpty

  def basketFull: Boolean = payments.size >= 5

  def getPcipalSessionLaunchResponse: PcipalSessionLaunchResponse = pcipalSessionLaunchResponse.getOrElse(
    throw new RuntimeException(s"Error: Missing PcipalSessionLaunchResponse in the journey [${journeyId.toString}]")
  )
  def getPaymentItem(paymentItemId: PaymentItemId): PaymentItem   = payments
    .find(_.paymentItemId === paymentItemId)
    .getOrElse(
      throw new RuntimeException(
        s"Error: Missing payment item identified by [${paymentItemId.toString}] [${journeyId.toString}]"
      )
    )
}

object Journey {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[Journey] = Json.format[Journey]
}
