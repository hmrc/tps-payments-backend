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

package support

import javax.inject.{Inject, Singleton}
import model.pcipal.{ChargeRefNotificationPcipalRequest, PcipalSessionId}
import model.{TpsId, TpsPaymentRequest, TpsPayments, UpdateRequest}
import uk.gov.hmrc.http.HttpReads.Implicits.{readFromJson, readRaw}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestConnector @Inject() (httpClient: HttpClient)(implicit executionContext: ExecutionContext) {

  private val port = 19001
  private val headers: Seq[(String, String)] = Seq(("Content-Type", "application/json"))

  def tpsPayments(launchRequest: TpsPaymentRequest)(implicit hc: HeaderCarrier): Future[TpsId] =
    httpClient.POST[TpsPaymentRequest, TpsId](s"http://localhost:$port/tps-payments-backend/tps-payments", launchRequest, headers)

  def store(tpsPayments: TpsPayments)(implicit hc: HeaderCarrier): Future[TpsId] =
    httpClient.POST[TpsPayments, TpsId](s"http://localhost:$port/tps-payments-backend/store", tpsPayments, headers)

  def find(id: TpsId)(implicit hc: HeaderCarrier): Future[TpsPayments] =
    httpClient.GET[TpsPayments](s"http://localhost:$port/tps-payments-backend/find/id/${id.value}", headers)

  def getId(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.GET[HttpResponse](s"http://localhost:$port/tps-payments-backend/get-id", headers)

  def delete(id: TpsId)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.DELETE[HttpResponse](s"http://localhost:$port/tps-payments-backend/delete/id/${id.value}", headers)

  def updateWithSessionId(id: TpsId, pId: PcipalSessionId)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.PATCH[UpdateRequest, HttpResponse](s"http://localhost:$port/tps-payments-backend/update-with-pid", UpdateRequest(id, pId), headers)

  def updateTpsPayments(chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.PATCH[ChargeRefNotificationPcipalRequest, HttpResponse](s"http://localhost:$port/tps-payments-backend/update-with-pcipal-data", chargeRefNotificationPciPalRequest, headers)

}
