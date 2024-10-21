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

package email

import email.model.EmailSendRequest
import play.api.Logger
import play.api.libs.json.Json
import tps.model.Email
import uk.gov.hmrc.http.HttpReads.Implicits.readUnit
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
final class EmailConnector @Inject() (
    httpClient:     HttpClientV2,
    servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext) {

  //TODO: refactor so it doesn't take dozen String parameters
  def sendEmail(emailAddress: Email, totalAmountPaid: String, transactionReference: String, cardType: String, cardNumber: String, tpsPaymentItemsForEmail: String)(implicit headerCarrier: HeaderCarrier): Future[Unit] = {
    logger.info("sending email ...")
    httpClient
      .post(url"$sendEmailUrl")
      .withBody(Json.toJson(EmailSendRequest(
        Seq(emailAddress),
        "telephone_payments_service",
        parameters = Map(
          "transactionReference" -> transactionReference,
          "totalAmountPaid" -> totalAmountPaid,
          "cardType" -> cardType,
          "cardNumber" -> cardNumber,
          "tpsPaymentItemsForEmail" -> tpsPaymentItemsForEmail
        )
      )))
      .execute[Unit]
  }

  private val sendEmailUrl: String = servicesConfig.baseUrl("email") + "/hmrc/email"

  private lazy val logger: Logger = Logger(this.getClass)
}
