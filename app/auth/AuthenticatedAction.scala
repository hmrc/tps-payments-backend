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

import auth.UnhappyPathResponses.{notLoggedIn, unauthorised}
import config.AppConfig
import javax.inject._
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedAction @Inject() (
    af:        AuthorisedFunctions,
    cc:        MessagesControllerComponents,
    appConfig: AppConfig)(implicit ec: ExecutionContext) extends ActionBuilder[Request, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers)

    af.authorised(AuthProviders(PrivilegedApplication)).retrieve(allEnrolments) {
      case allEnrols if allEnrols.enrolments.map(_.key).contains(appConfig.strideRole) =>
        block(request)
      case _ =>
        Logger.warn(s"user logged in with no credentials")
        Future successful unauthorised
    }.recover {
      case _: NoActiveSession =>
        Logger.warn(s"no active session")
        notLoggedIn
      case e: AuthorisationException =>
        Logger.debug(s"Unauthorised because of ${e.reason}, $e")
        unauthorised
    }

  }

  override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

  override protected def executionContext: ExecutionContext = cc.executionContext

}
