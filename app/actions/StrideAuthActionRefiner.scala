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

package actions

import actions.UnhappyPathResponses.{notLoggedIn, unauthorised}
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.credentials
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

class StrideAuthActionRefiner @Inject() (cc: MessagesControllerComponents, authConnector: AuthConnector)(implicit
  ec: ExecutionContext
) extends ActionRefiner[Request, AuthenticatedRequest] { self =>

  override def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    val predicate = Enrolment("tps_payment_taker_call_handler") and AuthProviders(PrivilegedApplication)

    af
      .authorised(predicate)
      .retrieve(credentials) {
        case Some(credentials) => Future.successful(Right(new AuthenticatedRequest(request, credentials)))
        case None              => Future.successful(Left(unauthorised))
      }
      .recover {
        case _: NoActiveSession        =>
          logger.warn(s"Unauthorised, no active session")
          Left(notLoggedIn)
        case e: AuthorisationException =>
          logger.info(s"Unauthorised because of ${e.reason}, ${e.toString}")
          Left(unauthorised)
      }
  }

  override protected def executionContext: ExecutionContext = cc.executionContext

  private val af: AuthorisedFunctions = new AuthorisedFunctions {
    override def authConnector: AuthConnector = self.authConnector
  }
  private lazy val logger: Logger     = Logger(this.getClass)
}
