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

package testsupport.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.UrlPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import tps.testdata.TdAll

object AuthStub {
  private val expectedDetail                  = "SessionRecordNotFound"
  private val authoriseUrlPattern: UrlPattern = urlEqualTo("/auth/authorise")

  /** The user is authenticated and authorised
    */
  def authorised(tdAll: TdAll = TdAll): StubMapping = {
    val authProvider: String = "PrivilegedApplication"
    val strideUserId: String = tdAll.pid

    stubFor(
      post(authoriseUrlPattern)
        .withRequestBody(
          equalToJson(
            // language=JSON
            s"""
           {
             "authorise": [
               {
                "identifiers":[],
                "state":"Activated",
                "enrolment":"tps_payment_taker_call_handler"
               },
               {
                 "authProviders": [
                   "$authProvider"
                 ]
               }
             ],
             "retrieve": [
               "optionalCredentials"
             ]
           }""",
            true,
            true
          )
        )
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(
              // language=JSON
              s"""
           {
             "optionalCredentials":{
               "providerId": "$strideUserId",
               "providerType": "$authProvider"
             }
           }
     """.stripMargin
            )
        )
    )
  }

  def notAuthenticated(): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(401)
            .withHeader("WWW-Authenticate", s"""MDTP detail="$expectedDetail"""")
        )
    )

  def notAuthorised(error: String = "clump"): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(401)
            .withHeader("WWW-Authenticate", s"""MDTP detail="$error"""")
        )
    )

}
