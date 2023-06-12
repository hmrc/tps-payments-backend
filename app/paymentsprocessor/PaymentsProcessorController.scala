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

package paymentsprocessor

import model.PaymentItemId
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PaymentsProcessorController @Inject() (
    cc:                       ControllerComponents,
    paymentsProcessorService: PaymentsProcessorService
)(implicit executionContext: ExecutionContext) extends BackendController(cc) {

  private[PaymentsProcessorController] val logger: Logger = Logger(this.getClass)

  def findModsSpecificData(paymentItemId: PaymentItemId): Action[AnyContent] = Action.async { _ =>
    for {
      modsPaymentCallBackRequest: ModsPaymentCallBackRequest <- paymentsProcessorService.findModsPaymentsByReference(paymentItemId)
      _ = logger.debug("Response to /payment-items/:id/mods-amendment-ref call: " + modsPaymentCallBackRequest.toString)
    } yield Ok(Json.toJson(modsPaymentCallBackRequest))
  }

}
