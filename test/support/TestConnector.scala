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

package support

import model._
import model.pcipal.ChargeRefNotificationPcipalRequest
import paymentsprocessor.ModsPaymentCallBackRequest
import recon.FindRPaymentSpecificDataRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import util.HttpReadsInstances._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestConnector @Inject() (httpClient: HttpClient)(implicit executionContext: ExecutionContext) {

  private val port = 19001
  private val headers: Seq[(String, String)] = Seq(("Content-Type", "application/json"))

  def startTpsJourneyMibOrPngr(launchRequest: TpsPaymentRequest)(implicit hc: HeaderCarrier): Future[TpsId] =
    httpClient.POST[TpsPaymentRequest, TpsId](s"http://localhost:$port/tps-payments-backend/tps-payments", launchRequest, headers)

  def upsert(tpsPayments: TpsPayments)(implicit hc: HeaderCarrier): Future[Unit] =
    httpClient.POST[TpsPayments, Unit](s"http://localhost:$port/tps-payments-backend/tps-payments/upsert", tpsPayments, headers)

  def find(id: TpsId)(implicit hc: HeaderCarrier): Future[TpsPayments] =
    httpClient.GET[TpsPayments](s"http://localhost:$port/tps-payments-backend/tps-payments/${id.value}", headers)

  def getPaymentItemTaxType(id: PaymentItemId)(implicit hc: HeaderCarrier): Future[TaxType] =
    httpClient.GET[TaxType](s"http://localhost:$port/tps-payments-backend/payment-items/${id.value}/tax-type", headers)

  def getModsPaymentItemAmendmentReference(id: PaymentItemId)(implicit hc: HeaderCarrier): Future[ModsPaymentCallBackRequest] =
    httpClient.GET[ModsPaymentCallBackRequest](s"http://localhost:$port/tps-payments-backend/payment-items/${id.value}/mods-amendment-ref", headers)

  def updateTpsPayments(chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.PATCH[ChargeRefNotificationPcipalRequest, HttpResponse](s"http://localhost:$port/tps-payments-backend/update-with-pcipal-data", chargeRefNotificationPciPalRequest, headers)

  def findModsPayments(findRPaymentSpecificDataRequest: FindRPaymentSpecificDataRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.POST[FindRPaymentSpecificDataRequest, HttpResponse](s"http://localhost:$port/tps-payments-backend/payments-recon/find-mods-data", findRPaymentSpecificDataRequest, headers)

}
