/*
 * Copyright 2022 HM Revenue & Customs
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

  def sendEmail(languageCode: String, email: String, displayTaxType: String, paymentReference: String, amountPaid: BigDecimal)(implicit headerCarrier: HeaderCarrier): Future[Unit] = {
    
    http.POST[EmailSendRequest, Unit](
      appConfig.emailServiceUrl,
      EmailSendRequest(
        Seq(email),
        if (languageCode.equals("cy")) "open_banking_payment_successful_cy" else "open_banking_payment_successful",
        parameters = Map(
          "taxType" -> displayTaxType,
          "reference" -> paymentReference,
          "amountPaid" -> amountPaid.toString
        )
      )
    )
    Future.successful(())
  }
}
