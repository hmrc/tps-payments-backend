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
import play.api.libs.json.Json
import recon.FindRPaymentSpecificDataRequest
import tps.journey.model.{Journey, JourneyId}
import tps.model.{PaymentItemId, TaxType}
import tps.pcipalmodel.ChargeRefNotificationPcipalRequest
import tps.startjourneymodel.StartJourneyRequestMibOrPngr
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import util.HttpReadsInstances._
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

//TODO: document who is calling this, also no need tests for that code as it's extra cost to maintain it
@Singleton
class TestConnector @Inject() (httpClient: HttpClientV2)(using ec: ExecutionContext):

  private val port                      = 19001
  private val headers: (String, String) = ("Content-Type", "application/json")

  def startTpsJourneyMibOrPngr(
    launchRequest: StartJourneyRequestMibOrPngr
  )(implicit hc: HeaderCarrier): Future[JourneyId] =
    httpClient
      .post(url"http://localhost:${port.toString}/tps-payments-backend/tps-payments")
      .withBody(Json.toJson(launchRequest))
      .setHeader(headers)
      .execute[JourneyId]

  def upsert(tpsPayments: Journey)(implicit hc: HeaderCarrier): Future[Unit] =
    httpClient
      .post(url"http://localhost:${port.toString}/tps-payments-backend/journey")
      .withBody(Json.toJson(tpsPayments))
      .setHeader(headers)
      .execute[Unit]

  def find(id: JourneyId)(implicit hc: HeaderCarrier): Future[Journey] =
    httpClient
      .get(url"http://localhost:${port.toString}/tps-payments-backend/journey/${id.value}")
      .setHeader(headers)
      .execute[Journey]

  def getPaymentItemTaxType(id: PaymentItemId)(implicit hc: HeaderCarrier): Future[TaxType] =
    httpClient
      .get(url"http://localhost:${port.toString}/tps-payments-backend/payment-items/${id.value}/tax-type")
      .setHeader(headers)
      .execute[TaxType]

  def getModsPaymentItemAmendmentReference(
    id: PaymentItemId
  )(implicit hc: HeaderCarrier): Future[ModsPaymentCallBackRequest] =
    httpClient
      .get(url"http://localhost:${port.toString}/tps-payments-backend/payment-items/${id.value}/mods-amendment-ref")
      .setHeader(headers)
      .execute[ModsPaymentCallBackRequest]

  def updateTpsPayments(
    chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest
  )(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .patch(url"http://localhost:${port.toString}/tps-payments-backend/update-with-pcipal-data")
      .withBody(Json.toJson(chargeRefNotificationPciPalRequest))
      .setHeader(headers)
      .execute[HttpResponse]

  def findModsPayments(
    findRPaymentSpecificDataRequest: FindRPaymentSpecificDataRequest
  )(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .post(url"http://localhost:${port.toString}/tps-payments-backend/payments-recon/find-mods-data")
      .withBody(Json.toJson(findRPaymentSpecificDataRequest))
      .setHeader(headers)
      .execute[HttpResponse]
