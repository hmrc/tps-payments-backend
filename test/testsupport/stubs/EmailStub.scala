/*
 * Copyright 2026 HM Revenue & Customs
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
import com.github.tomakehurst.wiremock.matching.{StringValuePattern, UrlPathPattern}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import email.model.EmailSendRequest
import tps.model.Email
import play.api.libs.json.Json

object EmailStub:
  private val emailUrl: UrlPathPattern = urlPathEqualTo("/hmrc/email")

  def sendEmail(welsh: Boolean = false): StubMapping =
    stubFor(
      post(emailUrl)
        .withRequestBody(emailBodyJson(welsh))
        .withHeader("Content-Type", equalTo("application/json"))
        .willReturn(aResponse().withStatus(200))
    )

  def verifySendEmail(welsh: Boolean = false, count: Int = 1): Unit =
    verify(
      count,
      postRequestedFor(emailUrl)
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(emailBodyJson(welsh))
    )

  private def emailBodyJson(welsh: Boolean): StringValuePattern =
    val templateId = if welsh then "telephone_payments_service_cy" else "telephone_payments_service"
    equalToJson(
      Json
        .toJson(
          EmailSendRequest(
            to = Seq(Email("test@email.com")),
            templateId = templateId,
            parameters = Map(
              "totalAmountPaid"         -> "104.04",
              "tpsPaymentItemsForEmail" -> """[{"taxType":"Self Assessment","amount":"104.04","transactionFee":"0.00","transactionNumber":"3123456701"}]""",
              "cardType"                -> "VISA",
              "transactionReference"    -> "31234567",
              "cardNumber"              -> "1234"
            ),
            force = false
          )
        )
        .toString(),
      true,
      true
    )
