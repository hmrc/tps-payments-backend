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

import javax.inject.{Inject, Singleton}
import model.{PaymentItemId, TpsPaymentItem, TpsPayments}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repository.TpsRepo
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class TestController @Inject() (cc: ControllerComponents, tpsRepo: TpsRepo)(implicit ec: ExecutionContext) extends BackendController(cc) {

  val possibleReferences = Seq("TT999991C", "TT999992C", "TT999993C", "TT999994C",
    "TT999995C", "TT999996C", "TT999997C", "TT999998C", "TT999999C")
  def removeTestData(): Action[AnyContent] = Action.async { implicit request =>

    for (
      result <- tpsRepo.removeByReferenceForTest(possibleReferences.toList)
    ) yield (Ok("Test data removed"))

  }

  def findByReference(ref: String): Action[AnyContent] = Action.async { implicit request =>

    for (
      result <- tpsRepo.findByReferenceForTest(ref)
    ) yield (Ok(Json.toJson(result)))

  }

  def storeTpsPayments(): Action[TpsPayments] = Action.async(parse.json[TpsPayments]) { implicit request =>

    val updatedPayments: List[TpsPaymentItem] = request.body.payments map (payment => payment.copy(paymentItemId = Some(PaymentItemId.fresh)))
    for {
      _ <- tpsRepo.upsert(request.body._id, request.body.copy(payments = updatedPayments))
    } yield {
      Ok(Json.toJson(request.body._id))
    }
  }

}
