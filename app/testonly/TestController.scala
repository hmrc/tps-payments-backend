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

package testonly

import deniedrefs.DeniedRefsRepo
import journey.JourneyService
import org.bson.types.ObjectId
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import tps.journey.model.{Journey, JourneyId}
import tps.model.{PaymentItem, PaymentItemId}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TestController @Inject() (
    deniedRefsRepo: DeniedRefsRepo,
    cc:             ControllerComponents,
    journeyService: JourneyService
)(implicit ec: ExecutionContext) extends BackendController(cc) {

  def findById(journeyId: JourneyId): Action[AnyContent] = Action.async {
    journeyService.find(journeyId).map(result => Ok(toJson(result)))
  }

  def storeTpsPayments(): Action[Journey] = Action.async(parse.json[Journey]) { implicit request =>
    val updatedPayments: List[PaymentItem] = request.body.payments map (payment => payment.copy(paymentItemId = PaymentItemId(ObjectId.get().toHexString)))
    journeyService.upsert(request.body.copy(payments = updatedPayments)).map { _ =>
      Ok(toJson(request.body._id))
    }
  }

  def dropDeniedRefsDb(): Action[AnyContent] = Action.async { _ =>
    for {
      result <- deniedRefsRepo.drop()
    } yield Ok(Json.obj("denied-refs-collection-dropped" -> result))
  }

}
