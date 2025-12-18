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

import actions.Actions
import config.AppConfig
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, ControllerComponents}
import tps.journey.model._
import tps.model._
import tps.startjourneymodel.{StartJourneyRequestMib, StartJourneyRequestMibOrPngr, StartJourneyRequestPngr}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.{Clock, Instant}
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StartJourneyController @Inject() (
  actions:                Actions,
  cc:                     ControllerComponents,
  journeyService:         JourneyService,
  paymentItemIdGenerator: PaymentItemIdGenerator,
  journeyIdGenerator:     JourneyIdGenerator,
  appConfig:              AppConfig,
  clock:                  Clock
)(using ec: ExecutionContext)
    extends BackendController(cc):

  val startJourneyMibOrPngr: Action[StartJourneyRequestMibOrPngr] =
    actions.strideAuthenticated.async(parse.json[StartJourneyRequestMibOrPngr]) { implicit request =>
      val startJourneyRequestMibOrPngr: StartJourneyRequestMibOrPngr = request.body
      val journey: Journey                                           = makeJourney(startJourneyRequestMibOrPngr)
      journeyService.upsert(journey).map { _ =>
        Created(toJson(journey._id))
      }
    }

  val startJourneyMib: Action[StartJourneyRequestMib] =
    actions.strideAuthenticated.async(parse.json[StartJourneyRequestMib]) { implicit request =>
      val journeyId        = journeyIdGenerator.nextId()
      logger.info(s"Starting MIB journey [${journeyId.toString}] ...")
      val sjr              = request.body
      val journey: Journey =
        Journey(
          _id = journeyId,
          journeyState = JourneyState.Started,
          pid = request.credentials.providerId,
          created = Instant.now(clock),
          payments = List(
            PaymentItem(
              paymentItemId = paymentItemIdGenerator.nextId(),
              amount = sjr.amount,
              headOfDutyIndicator = HeadOfDutyIndicators.B,
              updated = Instant.now(clock),
              customerName = sjr.customerName,
              chargeReference = sjr.mibReference,
              pcipalData = None,
              paymentSpecificData = MibSpecificData(
                chargeReference = sjr.mibReference,
                vat = sjr.totalVatDue,
                customs = sjr.totalDutyDue,
                amendmentReference = sjr.amendmentReference
              ),
              taxType = TaxTypes.MIB,
              email = None
            )
          ),
          navigation = Navigation(
            back = sjr.backUrl,
            reset = sjr.resetUrl,
            finish = sjr.finishUrl,
            callback = appConfig.paymentNotificationUrl
          )
        )

      journeyService
        .upsert(journey)
        .map { _ =>
          val startJourneyResponse: StartJourneyResponse = StartJourneyResponse(
            journeyId = journey.id,
            nextUrl = s"${appConfig.tpsFrontendBaseUrl}/tps-payments/make-payment/mib/${journey.id.value}"
          )
          Created(toJson(startJourneyResponse))
        }
    }

  val startJourneyPngr: Action[StartJourneyRequestPngr] =
    actions.strideAuthenticated.async(parse.json[StartJourneyRequestPngr]) { implicit request =>
      val journeyId        = journeyIdGenerator.nextId()
      logger.info(s"Starting PNGR journey [${journeyId.toString}] ...")
      val sjr              = request.body
      val journey: Journey =
        Journey(
          _id = journeyId,
          journeyState = JourneyState.Started,
          pid = request.credentials.providerId,
          created = Instant.now(clock),
          payments = List(
            PaymentItem(
              paymentItemId = paymentItemIdGenerator.nextId(),
              amount = sjr.amount,
              headOfDutyIndicator = HeadOfDutyIndicators.B,
              updated = Instant.now(clock),
              customerName = sjr.customerName,
              chargeReference = sjr.chargeReference,
              pcipalData = None,
              paymentSpecificData = PngrSpecificData(
                chargeReference = sjr.chargeReference
              ),
              taxType = TaxTypes.PNGR,
              email = None
            )
          ),
          navigation = Navigation(
            back = sjr.backUrl,
            reset = sjr.resetUrl,
            finish = sjr.finishUrl,
            callback = appConfig.paymentNotificationUrl
          )
        )

      journeyService
        .upsert(journey)
        .map { _ =>
          val startJourneyResponse: StartJourneyResponse = StartJourneyResponse(
            journeyId = journey.id,
            nextUrl = s"${appConfig.tpsFrontendBaseUrl}/tps-payments/make-payment/pngr/${journey.id.value}"
          )
          Created(toJson(startJourneyResponse))
        }
    }

  private def makeJourney(startJourneyRequestMibOrPngr: StartJourneyRequestMibOrPngr): Journey =
    val tpsPayments: List[PaymentItem] = startJourneyRequestMibOrPngr.payments.map { p =>
      PaymentItem(
        paymentItemId = paymentItemIdGenerator.nextId(),
        amount = p.amount,
        headOfDutyIndicator = HeadOfDutyIndicators.B,
        updated = Instant.now(clock),
        customerName = p.customerName,
        chargeReference = p.chargeReference,
        pcipalData = None,
        paymentSpecificData = p.paymentSpecificData,
        taxType = p.taxType,
        email = p.email
      )
    }.toList

    Journey(
      _id = journeyIdGenerator.nextId(),
      journeyState = JourneyState.Started,
      pid = startJourneyRequestMibOrPngr.pid,
      created = Instant.now(clock),
      payments = tpsPayments,
      navigation = startJourneyRequestMibOrPngr.navigation
    )

  private lazy val logger: Logger = Logger(this.getClass)
