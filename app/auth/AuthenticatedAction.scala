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

package auth

import auth.UnhappyPathResponses.{notLoggedIn, unauthorised}
import config.AppConfig

import javax.inject._
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedAction @Inject() (
    cc:            MessagesControllerComponents,
    appConfig:     AppConfig,
    val connector: AuthConnector)(implicit ec: ExecutionContext) extends ActionBuilder[Request, AnyContent] {

  private val af: AuthorisedFunctions = new AuthorisedFunctions {
    override def authConnector: AuthConnector = connector
  }

  val logger: Logger = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    val enrolments: Set[Predicate] = appConfig.strideRoles.map(Enrolment.apply)
    val stridePredicate: Predicate = enrolments.reduceOption(_ or _).getOrElse(EmptyPredicate)

    af.authorised(stridePredicate and AuthProviders(PrivilegedApplication))(block(request)).recover {
      case _: NoActiveSession =>
        logger.warn(s"no active session")
        notLoggedIn
      case e: AuthorisationException =>
        logger.debug(s"Unauthorised because of ${e.reason}, ${e.toString}")
        unauthorised
    }

  }

  override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

  override protected def executionContext: ExecutionContext = cc.executionContext

}
