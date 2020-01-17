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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.{HttpHeader, HttpHeaders}
import com.github.tomakehurst.wiremock.stubbing.StubMapping

object AuthWireMockResponses {

  val expectedDetail = "SessionRecordNotFound"

  def notAuthorised: StubMapping = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader("WWW-Authenticate", s"""MDTP detail="${expectedDetail}"""")
      )
    )
  }

  def authorised(authProvider: String, strideUserId: String): StubMapping = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .withRequestBody(
        equalToJson(
          //language=JSON
          s"""
             {
               "authorise": [
                 {
                   "authProviders": [
                     "$authProvider"
                   ]
                 }
               ],
             "retrieve" : [ "allEnrolments" ]
             }
           """.stripMargin, true, true))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(
          //language=JSON
          s"""
                    {"allEnrolments":[{"key":"tps_payments","identifiers":[],"state":"activated"}]}
       """.stripMargin)))

  }

  def failsWith(error: String): StubMapping = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader("WWW-Authenticate", s"""MDTP detail="$error"""")
      )
    )
  }

}
