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

package journey

import auth.Actions
import email.EmailService
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import tps.journey.model.{Journey, JourneyId, JourneyState}
import tps.model.PaymentItemId
import tps.pcipalmodel.ChargeRefNotificationPcipalRequest
import tps.startjourneymodel.StartJourneyRequestMibOrPngr
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import util.KibanaLogger

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyController @Inject() (actions:        Actions,
                                   cc:             ControllerComponents,
                                   emailService:   EmailService,
                                   journeyService: JourneyService)(implicit executionContext: ExecutionContext) extends BackendController(cc) {

  def startTpsJourneyMibOrPngr: Action[StartJourneyRequestMibOrPngr] = actions.strideAuthenticateAction().async(parse.json[StartJourneyRequestMibOrPngr]) { implicit request =>
    val journey: Journey = request.body.makeJourney(Instant.now())
    journeyService.upsert(journey).map { _ =>
      Created(toJson(journey._id))
    }
  }

  def upsert(): Action[Journey] = actions.strideAuthenticateAction().async(parse.json[Journey]) { implicit request =>
    val journey: Journey = request.body
    journeyService
      .upsert(journey)
      .map(_ => Ok)
  }

  def findJourney(journeyId: JourneyId): Action[AnyContent] = actions.strideAuthenticateAction().async {
    journeyService.find(journeyId).map {
      case Some(journey) => Ok(toJson(journey))
      case None          => NotFound(s"No journey with given id [${journeyId.value}]")
    }
  }

  def getTaxType(paymentItemId: PaymentItemId): Action[AnyContent] = Action.async {
    journeyService
      .findPaymentItem(paymentItemId)
      .map {
        case Some(paymentItem) => Ok(toJson(paymentItem.taxType))
        case None              => NotFound(s"No payment item found for given [paymentItemId:${paymentItemId.value}]")
      }
  }

  def updateWithPcipalData(): Action[ChargeRefNotificationPcipalRequest] = Action.async(parse.json[ChargeRefNotificationPcipalRequest]) { implicit request =>
    val notification: ChargeRefNotificationPcipalRequest = request.body
    KibanaLogger.info(
      message         = s"Update request from Pcipal received [paymentStatus: ${notification.Status.toString}][HoD:${notification.HoD.toString}]",
      journey         = None,
      pcipalSessionId = Some(notification.PCIPalSessionId),
      paymentItemId   = Some(notification.paymentItemId)
    )

    for {
      maybeJourney: JourneyService.FindByPcipalSessionIdResult <- journeyService.findByPcipalSessionId(notification.PCIPalSessionId, notification.paymentItemId)
      result <- maybeJourney match {

        case JourneyService.FindByPcipalSessionIdResult.Found(journey) =>
          val newJourney = journeyService
            .updateJourneyWithPcipalData(journey, notification)
            .copy(journeyState = JourneyState.ReceivedNotification)
          emailService.maybeSendEmail(newJourney)
          journeyService
            .upsert(newJourney)
            .map { _ =>
              KibanaLogger.info(
                message         = s"Journey updated with Pcipal data [paymentStatus: ${notification.Status.toString}",
                journey         = Some(newJourney),
                pcipalSessionId = Some(notification.PCIPalSessionId),
                paymentItemId   = Some(notification.paymentItemId)
              )
              Ok
            }

        case JourneyService.FindByPcipalSessionIdResult.NoJourneyBySessionId =>
          KibanaLogger.info("Update request from Pcipal resulted in NoJourneyBySessionId", None, Some(notification.PCIPalSessionId), Some(notification.paymentItemId))
          Future.successful(BadRequest(s"Could not find corresponding journey matching pcipalSessionId: [${notification.paymentItemId.value}] [PCIPalSessionId:${notification.PCIPalSessionId.value}] [HoD:${notification.HoD.toString}]"))

        case JourneyService.FindByPcipalSessionIdResult.NoMatchingPaymentItem(journey) =>
          KibanaLogger.info("Update request from Pcipal resulted in NoMatchingPaymentItem", Some(journey), Some(notification.PCIPalSessionId), Some(notification.paymentItemId))
          Future.successful(BadRequest(s"Could not find corresponding journey matching paymentItemId: [${notification.paymentItemId.value}] [PCIPalSessionId:${notification.PCIPalSessionId.value}] [HoD:${notification.HoD.toString}]"))
      }
    } yield result
  }

}
