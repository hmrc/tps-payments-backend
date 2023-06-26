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
import journey.JourneyRepo
import org.bson.types.ObjectId
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import tps.journey.model.Journey
import tps.model.{PaymentItem, PaymentItemId}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TestController @Inject() (
    deniedRefsRepo: DeniedRefsRepo,
    cc:             ControllerComponents,
    journeyRepo:    JourneyRepo)(implicit ec: ExecutionContext) extends BackendController(cc) {

  private val possibleReferences = Seq("TT999991", "TT999992", "TT999993", "TT999994",
    "TT999995", "TT999996", "TT999997", "TT999998", "TT999999")

  def removeTestData(): Action[AnyContent] = Action.async {
    journeyRepo.removeByReferenceForTest(possibleReferences.toList).map(_ => Ok("Test data removed"))
  }

  def findByReference(ref: String): Action[AnyContent] = Action.async {
    journeyRepo.findByReferenceForTest(ref).map(result => Ok(toJson(result)))
  }

  def storeTpsPayments(): Action[Journey] = Action.async(parse.json[Journey]) { implicit request =>
    val updatedPayments: List[PaymentItem] = request.body.payments map (payment => payment.copy(paymentItemId = Some(PaymentItemId(ObjectId.get().toHexString))))

    journeyRepo.upsert(request.body.copy(payments = updatedPayments)).map { _ =>
      Ok(toJson(request.body._id))
    }
  }

  def dropDeniedRefsDb(): Action[AnyContent] = Action.async { _ =>
    for {
      result <- deniedRefsRepo.drop()
    } yield Ok(Json.obj("denied-refs-collection-dropped" -> result))
  }

}
