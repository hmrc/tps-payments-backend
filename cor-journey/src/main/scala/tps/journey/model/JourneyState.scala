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

import julienrf.json.derived
import play.api.libs.json.Format
import tps.model.TaxType

sealed trait JourneyState

object JourneyState {

  sealed trait FinalState { self: JourneyState => }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: Format[JourneyState] = derived.oformat[JourneyState]()

  //Journey Started by Tps, on Landing page (or in MIB or in PNGR)
  final case object Landing extends JourneyState

  //Entering or Editing payment
  final case class EnterPayment(taxType: TaxType) extends JourneyState

  //Journey handed over to PciPal
  final case object AtPciPal extends JourneyState

  //User clicked reject button on the landing page
  final case object Rejected extends JourneyState with FinalState

  //User clicked reset at pcipal page
  final case object ResetByPciPal extends JourneyState with FinalState

  //User clicked Finish at pcipal page
  final case object FinishedByPciPal extends JourneyState with FinalState

  //User clicked back button on the PciPal. We finalize this journey and start a clone of it
  final case object BackByPciPal extends JourneyState with FinalState

  final case object ReceivedNotification extends JourneyState with FinalState
}
