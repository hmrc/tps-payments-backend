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

package connectors

import config.AppConfig

import javax.inject.{Inject, Singleton}
import model.EmailSendRequest
import uk.gov.hmrc.http.HttpReads.Implicits.readUnit
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
final class EmailConnector @Inject() (
    http:      HttpClient,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  def sendEmail(emailAddress: String, totalAmountPaid: String, transactionReference: String, cardType: String, cardNumber: String, tpsPaymentItemsForEmail: String)(implicit headerCarrier: HeaderCarrier): Future[Unit] = {
    http.POST[EmailSendRequest, Unit](
      appConfig.emailServiceUrl,
      EmailSendRequest(
        Seq(emailAddress),
        "telephone_payments_service",
        parameters = Map(
          "transactionReference" -> transactionReference,
          "totalAmountPaid" -> totalAmountPaid,
          "cardType" -> cardType,
          "cardNumber" -> cardNumber,
          "tpsPaymentItemsForEmail" -> tpsPaymentItemsForEmail
        )
      )
    )
    Future.successful(())
  }
}
