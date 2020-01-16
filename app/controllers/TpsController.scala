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

import auth.StrideAuthenticatedAction
import javax.inject.{Inject, Singleton}
import model.{TpsId, TpsPayments}
import play.api.mvc.{Action, ControllerComponents}
import repository.TpsRepo
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class TpsController @Inject() (
    cc:         ControllerComponents,
    tpsRepo:    TpsRepo,
    strideAuth: StrideAuthenticatedAction
)(
    implicit
    executionContext: ExecutionContext
) extends BackendController(cc) {
  def storeTpsPayments(): Action[TpsPayments] = strideAuth.async(parse.json[TpsPayments]) { implicit request =>

    val tpsId: TpsId = TpsId.fresh
    for {
      result <- tpsRepo.upsert(tpsId, request.body.copy(_id = Some(tpsId)))

    } yield {
      Ok(s"updated ${result.n.toString} records")
    }

  }
}
