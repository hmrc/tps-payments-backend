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

package tps.journey

import play.api.mvc.Request
import tps.journey.model.{Journey, JourneyId}
import tps.startjourneymodel.StartJourneyRequestMibOrPngr
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import tps.utils.RequestSupport._
import tps.utils.HttpReadsInstances._

class JourneyConnector(
    httpClient: HttpClient,
    baseUrl:    String
)(implicit ec: ExecutionContext) {

  def startMibOrPngrJourney(startJourneyRequest: StartJourneyRequestMibOrPngr)(implicit request: Request[_]): Future[JourneyId] = {
    httpClient.POST[StartJourneyRequestMibOrPngr, JourneyId](s"$baseUrl/tps-payments-backend/tps-payments", startJourneyRequest)
  }

  def upsert(journey: Journey)(implicit request: Request[_]): Future[Unit] = httpClient
    .POST[Journey, Unit](
      s"$baseUrl/tps-payments-backend/journey",
      journey
    )

  def find(id: JourneyId)(implicit request: Request[_]): Future[Option[Journey]] = httpClient
    .GET[Option[Journey]](s"$baseUrl/tps-payments-backend/journey/${id.value}")

  @Inject()
  def this(httpClient: HttpClient, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) = this(
    httpClient,
    servicesConfig.baseUrl("tps-payments-backend")
  )

}
