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

import journey.JourneyService
import tps.model.{MibSpecificData, PaymentItemId}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentsProcessorService @Inject() (journeyService: JourneyService)(implicit ec: ExecutionContext) {

  def getModsPaymentCallbackRequest(paymentItemId: PaymentItemId): Future[ModsPaymentCallBackRequest] =
    journeyService.findPaymentItem(paymentItemId).map {
      case Some(paymentItem) =>
        paymentItem.paymentSpecificData match {
          case paymentItem: MibSpecificData =>
            ModsPaymentCallBackRequest(paymentItem.chargeReference, paymentItem.amendmentReference)
          case _                            =>
            throw new RuntimeException(
              s"No payment items with this id [ ${paymentItemId.value} ], it's not mods, why is it being looked up?"
            )
        }
      case None              => throw new RuntimeException(s"No payment specific data for id [ ${paymentItemId.value} ]")
    }

}
