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

package tps

import play.api.mvc.Request
import tps.deniedrefsmodel.{VerifyRefsRequest, VerifyRefsResponse}
import tps.utils.HttpReadsInstances._
import tps.utils.RequestSupport._
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TpsPaymentsBackendConnector @Inject() (
    httpClient:     HttpClient,
    servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext) {

  private val serviceURL: String = servicesConfig.baseUrl("tps-payments-backend")

  def verifyRefDenyList(verifyRefsRequest: VerifyRefsRequest)(implicit request: Request[_]): Future[VerifyRefsResponse] =
    httpClient
      .POST[VerifyRefsRequest, VerifyRefsResponse](
        s"$serviceURL/tps-payments-backend/verify-refs",
        verifyRefsRequest
      )

}
