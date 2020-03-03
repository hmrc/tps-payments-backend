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

import auth.{Actions, UnhappyPathResponses}
import config.AppConfig
import javax.inject.{Inject, Singleton}
import model.{PaymentItemId, TpsId, TpsPaymentItem, TpsPayments}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repository.TpsRepo
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TpsController @Inject() (actions:              Actions,
                               cc:                   ControllerComponents,
                               tpsRepo:              TpsRepo,
                               appConfig:            AppConfig,
                               unhappyPathResponses: UnhappyPathResponses
)(
    implicit
    executionContext: ExecutionContext
) extends BackendController(cc) {

  def storeTpsPayments(): Action[TpsPayments] = actions.strideAuthenticateAction.async(parse.json[TpsPayments]) { implicit request =>

    val updatedPayments: List[TpsPaymentItem] = request.body.payments map (payment => payment.copy(paymentItemId = Some(PaymentItemId.fresh)))
    for {
      _ <- tpsRepo.upsert(request.body._id, request.body.copy(payments = updatedPayments))
    } yield {
      Ok(Json.toJson(request.body._id))
    }
  }

  def findTpsPayments(id: TpsId): Action[AnyContent] = actions.strideAuthenticateAction.async { implicit request =>
    Logger.debug(s"findTpsPayments received vrn ${id}")
    for {
      data <- tpsRepo.findPayment(id)
    } yield {
      data match {
        case Some(x) => Ok(Json.toJson(x))
        case None    => NotFound(s"No payments found for id ${id.value}")
      }

    }
  }

  def getId(): Action[AnyContent] = actions.strideAuthenticateAction.async { implicit request =>
    Logger.debug(s"getId")
    Future.successful(Ok(Json.toJson(TpsId.fresh)))
  }

  def delete(tpsId: TpsId) = actions.strideAuthenticateAction.async { implicit request =>
    Logger.debug(s"delete, id= ${tpsId.value}")
    for {
      del <- tpsRepo.removeById(tpsId)
    } yield {
      Ok
    }
  }

}
