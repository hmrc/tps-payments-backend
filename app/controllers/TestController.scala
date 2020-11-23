/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDateTime

import javax.inject.{Inject, Singleton}
import model.{PaymentItemId, TpsPaymentItem, TpsPaymentRequest, TpsPayments}
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repository.TpsRepo
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class TestController @Inject() (cc: ControllerComponents, tpsRepo: TpsRepo)(implicit ec: ExecutionContext) extends BackendController(cc) {

  val possibleReferences = Seq("TT999991", "TT999992", "TT999993", "TT999994",
    "TT999995", "TT999996", "TT999997", "TT999998", "TT999999")

  def removeTestData(): Action[AnyContent] = Action.async {
    tpsRepo.removeByReferenceForTest(possibleReferences.toList).map(_ => Ok("Test data removed"))
  }

  def findByReference(ref: String): Action[AnyContent] = Action.async {
    tpsRepo.findByReferenceForTest(ref).map(result => Ok(toJson(result)))
  }

  def storeTpsPayments(): Action[TpsPayments] = Action.async(parse.json[TpsPayments]) { implicit request =>
    val updatedPayments: List[TpsPaymentItem] = request.body.payments map (payment => payment.copy(paymentItemId = Some(PaymentItemId.fresh)))

    tpsRepo.upsert(request.body._id, request.body.copy(payments = updatedPayments)).map { _ =>
      Ok(toJson(request.body._id))
    }
  }

  def createTpsPayments: Action[TpsPaymentRequest] = Action.async(parse.json[TpsPaymentRequest]) { implicit request =>
    val tpsPayments = request.body.tpsPayments(LocalDateTime.now())

    tpsRepo.upsert(tpsPayments._id, tpsPayments).map { _ =>
      Created(toJson(tpsPayments._id))
    }
  }
}
