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

import config.AppConfig
import javax.inject._
import play.api.Logger
import play.api.libs.json.Reads
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthActions @Inject() (
    val authConnector:    AuthConnector,
    cc:                   ControllerComponents,
    appConfig:            AppConfig,
    unhappyPathResponses: UnhappyPathResponses
)(
    implicit
    executionContext: ExecutionContext
)
  extends BackendController(cc) with AuthorisedFunctions {

  def strideAuthenticate[T](body: Request[T] => Future[Result])(implicit reads: Reads[T]): Action[T] =
    Action.async(parse.json[T]) { implicit request =>
      authorised(AuthProviders(PrivilegedApplication)).retrieve(allEnrolments) {
        case allEnrols if allEnrols.enrolments.map(_.key).contains(appConfig.strideRole) =>
          body(request)
        case e => {
          Logger.warn(s"user logged in with no credentials")
          Future successful unhappyPathResponses.unauthorised
        }
      }.recover {
        case _: NoActiveSession => {
          Logger.warn(s"no active session")
          unhappyPathResponses.notLoggedIn
        }
        case e: AuthorisationException =>
          Logger.debug(s"Unauthorised because of ${e.reason}, $e")
          unhappyPathResponses.unauthorised
      }

    }

}
