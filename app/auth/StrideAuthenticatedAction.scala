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

package auth

import com.google.inject.Inject
import config.AppConfig
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.credentials
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.{ExecutionContext, Future}

class StrideAuthenticatedAction @Inject() (
    af:           AuthorisedFunctions,
    appConfig:    AppConfig,
    badResponses: UnhappyPathResponses,
    cc:           ControllerComponents)(implicit ec: ExecutionContext) extends ActionBuilder[AuthenticatedRequest, AnyContent] with AuthRedirects {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    implicit val r: Request[A] = request

    val strideEnrolment = Enrolment(appConfig.strideRole)

    af.authorised((strideEnrolment) and AuthProviders(PrivilegedApplication)).retrieve(credentials) {
      case Some(creds) =>
        block(new AuthenticatedRequest(request, creds))
      case None =>
        Logger.warn(s"user logged in with no credentials")
        Future.successful(badResponses.unauthorised)
    }.recover {
      case _: NoActiveSession => {
        Logger.warn(s"no active session")
        badResponses.notLoggedIn
      }
      case e: AuthorisationException =>
        Logger.debug(s"Unauthorised because of ${e.reason}, $e")
        badResponses.unauthorised
    }
  }

  override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

  override protected def executionContext: ExecutionContext = ec

  override def env: Environment = appConfig.runModeEnvironment

  override def config: Configuration = appConfig.runTimeConfig
}
