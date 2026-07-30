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

package journey

import testsupport.ItSpec
import testsupport.testdata.TestData.*
import _root_.journey.payments.FindPaymentsResponse.Payment
import _root_.journey.payments.{FindPaymentsRequest, FindPaymentsResponse}
import tps.journey.model.JourneyId
import tps.model.PaymentItemId
import tps.pcipalmodel.PcipalSessionId
import tps.testdata.TdAll

class JourneyServiceSpec extends ItSpec {

  "findByPcipalSessionId should throw error when more than one payment found" in {
    Option(journeyRepo.upsert(tpsPaymentsWithPcipalData).futureValue.getUpsertedId).isDefined shouldBe true
    Option(
      journeyRepo
        .upsert(tpsPaymentsWithPcipalData.copy(_id = JourneyId("session-48c978bb-64b6-4a00-a1f1-51e267some-new-one")))
        .futureValue
        .getUpsertedId
    ).isDefined shouldBe true
    val tpsPaymentId: PaymentItemId = tpsPaymentsWithPcipalData.payments.headOption.value.paymentItemId
    intercept[Exception] {
      journeyService.findByPcipalSessionId(PcipalSessionId("48c978bb"), tpsPaymentId).futureValue
    }.getMessage should include("Found 2 journeys with given pcipalSessionId [48c978bb]")
  }

  "findPaymentItem should optionally find the matching payment item" in {
    journeyService.findPaymentItem(paymentItemId).futureValue shouldBe None
    journeyService.upsert(journey).futureValue
    journeyService.findPaymentItem(paymentItemId).futureValue.value shouldBe journey.payments.headOption.value
  }

  "upsert should encrypt relevant fields in journey" in {
    val journeyBeforeEncryption   = tpsPaymentsWithPcipalData
    journeyService.upsert(journeyBeforeEncryption).futureValue
    val journeyInMongo            = journeyRepo.findById(journeyBeforeEncryption.journeyId).futureValue
    journeyInMongo should not be journeyBeforeEncryption withClue "some fields in the journey should be encrypted"
    val sensitiveStringsInJourney =
      List("some test name", "test@email.com", "chargeReference", "1234567895K")
    sensitiveStringsInJourney.foreach { sensitiveData =>
      journeyBeforeEncryption.toString should include(
        sensitiveData
      ) withClue "the strings should be in the unencrypted journey..."
      journeyInMongo.toString should not include sensitiveData withClue "there were unencrypted values in the 'encrypted' journey..."
    }
  }

  "findPayments" - {

    "FindPaymentsResponse" - {
      "return journey when one is found for given searchTag" in {
        val testJourney      = TdAll.TdJourneySa.journeyReceivedNotification
        val expectedResponse = FindPaymentsResponse(
          Seq(
            FindPaymentsResponse.Payment(
              reference = "1234567895",
              transactionReference = "1234567895K",
              amountInPence = 10404,
              createdOn = frozenInstant,
              taxType = "Sa"
            )
          )
        )

        journeyService.upsert(testJourney).futureValue

        val testRequest = FindPaymentsRequest(Seq("1234567895"), 1)
        val result      = journeyService.findPayments(testRequest).futureValue
        result shouldEqual expectedResponse
      }

      "return empty collection of journeys when none are found" in {
        val expectedResponse = FindPaymentsResponse(Seq.empty[Payment])
        val testRequest      = FindPaymentsRequest(Seq("1234567895"), 1)
        val result           = journeyService.findPayments(testRequest).futureValue
        result shouldEqual expectedResponse
      }
    }
  }

}
