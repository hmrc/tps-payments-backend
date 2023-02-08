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

package controllers

import play.api.http.Status
import recon.FindRPaymentSpecificDataRequest
import support.testdata.TestData._
import support.{ItSpec, TestConnector}
import uk.gov.hmrc.http.HeaderCarrier

class ReconControllerSpec extends ItSpec with Status {

  private implicit val emptyHC: HeaderCarrier = HeaderCarrier()

  private lazy val connector = injector.instanceOf[TestConnector]

  "Recon connector should surface mods info correctly when they exist" in {
    repo.upsert(modsTpsPaymentsNoAmendmentReference).futureValue
    connector.findModsPayments(FindRPaymentSpecificDataRequest(modsLookupChargeRefs)).futureValue.body shouldBe modsReconLookupJson.toString()
  }

}
