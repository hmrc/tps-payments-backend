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

package tps.deniedrefs

import play.api.libs.json.Json
import play.api.mvc.Request
import tps.deniedrefs.model.{VerifyRefsRequest, VerifyRefsResponse}
import tps.utils.HttpReadsInstances._
import tps.utils.RequestSupport._
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyRefsConnector @Inject() (
    httpClient:     HttpClientV2,
    servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext) {

  private val serviceURL: String = servicesConfig.baseUrl("tps-payments-backend")

  def verifyRefs(verifyRefsRequest: VerifyRefsRequest)(implicit request: Request[_]): Future[VerifyRefsResponse] =
    httpClient
      .post(url"$serviceURL/tps-payments-backend/verify-refs")
      .withBody(Json.toJson(verifyRefsRequest))
      .execute[VerifyRefsResponse]

}
