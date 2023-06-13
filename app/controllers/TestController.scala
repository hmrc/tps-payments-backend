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

package controllers

import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repository.JourneyRepo
import tps.model.{Journey, PaymentItem, PaymentItemId}
import tps.startjourneymodel.StartJourneyRequest
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TestController @Inject() (cc: ControllerComponents, tpsRepo: JourneyRepo)(implicit ec: ExecutionContext) extends BackendController(cc) {

  private val possibleReferences = Seq("TT999991", "TT999992", "TT999993", "TT999994",
    "TT999995", "TT999996", "TT999997", "TT999998", "TT999999")

  def removeTestData(): Action[AnyContent] = Action.async {
    tpsRepo.removeByReferenceForTest(possibleReferences.toList).map(_ => Ok("Test data removed"))
  }

  def findByReference(ref: String): Action[AnyContent] = Action.async {
    tpsRepo.findByReferenceForTest(ref).map(result => Ok(toJson(result)))
  }

  def storeTpsPayments(): Action[Journey] = Action.async(parse.json[Journey]) { implicit request =>
    val updatedPayments: List[PaymentItem] = request.body.payments map (payment => payment.copy(paymentItemId = Some(PaymentItemId.fresh())))

    tpsRepo.upsert(request.body.copy(payments = updatedPayments)).map { _ =>
      Ok(toJson(request.body._id))
    }
  }

  def createTpsPayments: Action[StartJourneyRequest] = Action.async(parse.json[StartJourneyRequest]) { implicit request =>
    val tpsPayments: Journey = request.body.tpsPayments(Instant.now())

    tpsRepo.upsert(tpsPayments).map { _ =>
      Created(toJson(tpsPayments._id))
    }
  }

}
