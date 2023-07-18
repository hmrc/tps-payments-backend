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

package testsupport

import paymentsprocessor.ModsPaymentCallBackRequest
import recon.FindRPaymentSpecificDataRequest
import tps.journey.model.{Journey, JourneyId}
import tps.model.{PaymentItemId, TaxType}
import tps.pcipalmodel.ChargeRefNotificationPcipalRequest
import tps.startjourneymodel.StartJourneyRequestMibOrPngr
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import util.HttpReadsInstances._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

//TODO: document who is calling this, also no need tests for that code as it's extra cost to maintain it
@Singleton
class TestConnector @Inject() (httpClient: HttpClient)(implicit executionContext: ExecutionContext) {

  private val port = 19001
  private val headers: Seq[(String, String)] = Seq(("Content-Type", "application/json"))

  def startTpsJourneyMibOrPngr(launchRequest: StartJourneyRequestMibOrPngr)(implicit hc: HeaderCarrier): Future[JourneyId] =
    httpClient.POST[StartJourneyRequestMibOrPngr, JourneyId](s"http://localhost:${port.toString}/tps-payments-backend/tps-payments", launchRequest, headers)

  def upsert(tpsPayments: Journey)(implicit hc: HeaderCarrier): Future[Unit] =
    httpClient.POST[Journey, Unit](s"http://localhost:${port.toString}/tps-payments-backend/journey", tpsPayments, headers)

  def find(id: JourneyId)(implicit hc: HeaderCarrier): Future[Journey] =
    httpClient.GET[Journey](s"http://localhost:${port.toString}/tps-payments-backend/journey/${id.value}", headers)

  def getPaymentItemTaxType(id: PaymentItemId)(implicit hc: HeaderCarrier): Future[TaxType] =
    httpClient.GET[TaxType](s"http://localhost:${port.toString}/tps-payments-backend/payment-items/${id.value}/tax-type", headers)

  def getModsPaymentItemAmendmentReference(id: PaymentItemId)(implicit hc: HeaderCarrier): Future[ModsPaymentCallBackRequest] =
    httpClient.GET[ModsPaymentCallBackRequest](s"http://localhost:${port.toString}/tps-payments-backend/payment-items/${id.value}/mods-amendment-ref", headers)

  def updateTpsPayments(chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.PATCH[ChargeRefNotificationPcipalRequest, HttpResponse](s"http://localhost:${port.toString}/tps-payments-backend/update-with-pcipal-data", chargeRefNotificationPciPalRequest, headers)

  def findModsPayments(findRPaymentSpecificDataRequest: FindRPaymentSpecificDataRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.POST[FindRPaymentSpecificDataRequest, HttpResponse](s"http://localhost:${port.toString}/tps-payments-backend/payments-recon/find-mods-data", findRPaymentSpecificDataRequest, headers)

}
