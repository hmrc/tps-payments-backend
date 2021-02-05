/*
 * Copyright 2021 HM Revenue & Customs
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

package recon

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class ReconController @Inject() (cc: ControllerComponents, reconService: ReconService)(implicit executionContext: ExecutionContext) extends BackendController(cc) {

  val logger: Logger = Logger(this.getClass)

  def findModsPayments(): Action[FindRPaymentSpecificDataRequest] = Action.async(parse.json[FindRPaymentSpecificDataRequest]) { implicit request =>
    logger.debug(s"findModsPayments [ ${request.toString} ]")
    for {
      transactions <- reconService.findModsPaymentsByReference(request.body.modsReferences)
    } yield Ok(Json.toJson(transactions))
  }

}
