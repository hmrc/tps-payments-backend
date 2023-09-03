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
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, ControllerComponents}
import tps.journey.model.{Journey, JourneyIdGenerator, JourneyState, PaymentItemIdGenerator}
import tps.model.{HeadOfDutyIndicators, PaymentItem}
import tps.startjourneymodel.StartJourneyRequestMibOrPngr
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.{Clock, Instant}
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StartJourneyController @Inject() (actions:                Actions,
                                        cc:                     ControllerComponents,
                                        journeyService:         JourneyService,
                                        paymentItemIdGenerator: PaymentItemIdGenerator,
                                        journeyIdGenerator:     JourneyIdGenerator,
                                        clock:                  Clock)(implicit executionContext: ExecutionContext) extends BackendController(cc) {

  def startJourneyMibOrPngr: Action[StartJourneyRequestMibOrPngr] = actions.strideAuthenticateAction().async(parse.json[StartJourneyRequestMibOrPngr]) { implicit request =>
    val startJourneyRequestMibOrPngr: StartJourneyRequestMibOrPngr = request.body
    logger.info(s"Starting MIB or PNGR journey [taxType:${startJourneyRequestMibOrPngr.paymentItem.taxType.toString}]")
    val journey: Journey = makeJourney(startJourneyRequestMibOrPngr)
    journeyService.upsert(journey).map { _ =>
      Created(toJson(journey._id))
    }
  }

  private def makeJourney(startJourneyRequestMibOrPngr: StartJourneyRequestMibOrPngr): Journey = {
    val tpsPayments: List[PaymentItem] = startJourneyRequestMibOrPngr.payments.map { p =>
      PaymentItem(
        paymentItemId       = paymentItemIdGenerator.nextId(),
        amount              = p.amount,
        headOfDutyIndicator = HeadOfDutyIndicators.B,
        updated             = Instant.now(clock),
        customerName        = p.customerName,
        chargeReference     = p.chargeReference,
        pcipalData          = None,
        paymentSpecificData = p.paymentSpecificData,
        taxType             = p.taxType,
        email               = p.email
      )
    }.toList

    Journey(
      _id          = journeyIdGenerator.nextId(),
      journeyState = JourneyState.Started,
      pid          = startJourneyRequestMibOrPngr.pid,
      created      = Instant.now(clock),
      payments     = tpsPayments,
      navigation   = startJourneyRequestMibOrPngr.navigation
    )
  }

  private lazy val logger: Logger = Logger(this.getClass)
}
