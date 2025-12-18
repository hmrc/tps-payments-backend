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
import tps.model.{PaymentItemId, TpsNativeTaxType}

sealed trait JourneyState derives CanEqual

object JourneyState:

  sealed trait FinalState { self: JourneyState => }

  given startedFormat: OFormat[Started.type]                           = Json.format[Started.type]
  given enterPaymentFormat: OFormat[EnterPayment]                      = Json.format[EnterPayment]
  given editPaymentFormat: OFormat[EditPayment]                        = Json.format[EditPayment]
  given atPciPalFormat: OFormat[AtPciPal.type]                         = Json.format[AtPciPal.type]
  given rejectedFormat: OFormat[Rejected.type]                         = Json.format[Rejected.type]
  given resetByPciPalFormat: OFormat[ResetByPciPal.type]               = Json.format[ResetByPciPal.type]
  given finishedByPciPalFormat: OFormat[FinishedByPciPal.type]         = Json.format[FinishedByPciPal.type]
  given backByPciPalFormat: OFormat[BackByPciPal.type]                 = Json.format[BackByPciPal.type]
  given receivedNotificationFormat: OFormat[ReceivedNotification.type] = Json.format[ReceivedNotification.type]

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[JourneyState] = new Format[JourneyState]:

    override def writes(o: JourneyState): JsValue = o match
      case s: Started.type              => Json.obj("Started" -> Json.toJson(s)(startedFormat))
      case e: EnterPayment              => Json.obj("EnterPayment" -> Json.toJson(e)(enterPaymentFormat))
      case e: EditPayment               => Json.obj("EditPayment" -> Json.toJson(e)(editPaymentFormat))
      case s: AtPciPal.type             => Json.obj("AtPciPal" -> Json.toJson(s)(atPciPalFormat))
      case s: Rejected.type             => Json.obj("Rejected" -> Json.toJson(s)(rejectedFormat))
      case s: ResetByPciPal.type        => Json.obj("ResetByPciPal" -> Json.toJson(s)(resetByPciPalFormat))
      case s: FinishedByPciPal.type     => Json.obj("FinishedByPciPal" -> Json.toJson(s)(finishedByPciPalFormat))
      case s: BackByPciPal.type         => Json.obj("BackByPciPal" -> Json.toJson(s)(backByPciPalFormat))
      case s: ReceivedNotification.type =>
        Json.obj("ReceivedNotification" -> Json.toJson(s)(receivedNotificationFormat))

    override def reads(json: JsValue): JsResult[JourneyState] =
      json match
        case JsObject(fields) =>
          fields.keys.headOption match
            case Some("Started" | "Landing" | "BasketNotEmpty") => JsSuccess(Started)
            case Some("EnterPayment")                           => Json.fromJson[EnterPayment](fields("EnterPayment"))(enterPaymentFormat)
            case Some("EditPayment")                            => Json.fromJson[EditPayment](fields("EditPayment"))(editPaymentFormat)
            case Some("AtPciPal")                               => JsSuccess(AtPciPal)
            case Some("Rejected")                               => JsSuccess(Rejected)
            case Some("ResetByPciPal")                          => JsSuccess(ResetByPciPal)
            case Some("FinishedByPciPal")                       => JsSuccess(FinishedByPciPal)
            case Some("BackByPciPal")                           => JsSuccess(BackByPciPal)
            case Some("ReceivedNotification")                   => JsSuccess(ReceivedNotification)
            case Some(other)                                    => JsError(s"Unknown JourneyState type: $other")
            case None                                           => JsError("Empty JSON object, expected JourneyState wrapper")
        case _                => JsError("Invalid JSON for JourneyState: expected JSON object")

  // Journey Started by Tps, on the Basket page (or in MIB or in PNGR)
  case object Started extends JourneyState

  // Entering Payment
  final case class EnterPayment(taxType: TpsNativeTaxType) extends JourneyState

  // Editing Payment
  final case class EditPayment(paymentItemId: PaymentItemId) extends JourneyState

  // Journey handed over to PciPal
  case object AtPciPal extends JourneyState

  // User clicked reject button on the landing page
  case object Rejected extends JourneyState with FinalState

  // User clicked reset at pcipal page
  case object ResetByPciPal extends JourneyState with FinalState

  // User clicked Finish at pcipal page
  case object FinishedByPciPal extends JourneyState with FinalState

  // User clicked back button on the PciPal. We finalize this journey and start a clone of it
  case object BackByPciPal extends JourneyState with FinalState

  case object ReceivedNotification extends JourneyState with FinalState
