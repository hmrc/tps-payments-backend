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

package support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.concurrent.Eventually

object AuthStub extends Eventually {
  private val expectedDetail = "SessionRecordNotFound"

  def givenTheUserIsNotAuthenticated(): StubMapping =
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader("WWW-Authenticate", s"""MDTP detail="$expectedDetail"""")
      )
    )

  def givenTheUserIsAuthenticatedAndAuthorised(): StubMapping = eventually {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .withRequestBody(
        equalToJson(
          //language=JSON
          """
              {
                "authorise": [
                  {
                    "$or": [
                      {
                        "enrolment": "digital_tps_payment_taker_call_handler",
                        "identifiers": [],
                        "state": "Activated"
                      },
                      {
                        "enrolment": "tps_payment_taker_call_handler",
                        "identifiers": [],
                        "state": "Activated"
                      }
                    ]
                  },
                  {
                    "authProviders": [
                      "PrivilegedApplication"
                    ]
                  }
                ],
                "retrieve": []
              }
           """, true, true))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(
          //language=JSON
          s"""
                    {"allEnrolments":[{"key":"digital_tps_payment_taker_call_handler","identifiers":[],"state":"activated"}]}
       """.stripMargin)))
  }

  def givenTheUserIsNotAuthorised(error: String): StubMapping =
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader("WWW-Authenticate", s"""MDTP detail="$error"""")
      )
    )

}
