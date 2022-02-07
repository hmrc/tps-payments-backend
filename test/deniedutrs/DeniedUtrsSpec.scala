/*
 * Copyright 2022 HM Revenue & Customs
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

package deniedutrs

import _root_.model.Utr
import deniedutrs.model._
import support.TestData._
import support.{ItSpec, TestData}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.Future

class DeniedUtrsSpec extends ItSpec {

  "upload denied utrs" in {
    val uploadResult1: UploadDeniedUtrsResponse = uploadDeniedUtrs(csvFile1).futureValue
    uploadResult1.size shouldBe 3
    val uploadResult2: UploadDeniedUtrsResponse = uploadDeniedUtrs(csvFile2).futureValue
    uploadResult2.size shouldBe 4

    uploadResult2.inserted isAfter uploadResult1.inserted shouldBe true withClue "second upload is newer"

  }

  "verify utrs" in {
    dropDb() withClue "given emtpy database"

    withClue("there is no information whether an utr is denied") {
      verifyUtrs().futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.MissingInformation)
      verifyUtrs(utr1, utr2, utr3).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.MissingInformation)
      verifyUtrs(utr2).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.MissingInformation)
      verifyUtrs(utr3).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.MissingInformation)
      verifyUtrs(utr4).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.MissingInformation)
    }

    withClue("after uploading csv with denied utrs some utrs should be denied") {
      uploadDeniedUtrs(csvFile1).futureValue.size shouldBe 3
      verifyUtrs().futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrPermitted)
      verifyUtrs(utr1, utr2, utr3).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied)
      verifyUtrs(utr2).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied)
      verifyUtrs(utr3).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied)
      verifyUtrs(utr4).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrPermitted)
      verifyUtrs(utr1, utr4).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied) withClue "one permitted one denied"
    }

    withClue("after uploading the second csv with denied utrs some other utrs should be denied") {

      uploadDeniedUtrs(csvFile2).futureValue.size shouldBe 4
      verifyUtrs(utr1).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrPermitted)
      verifyUtrs(utr2).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied)
      verifyUtrs(utr3).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied)
      verifyUtrs(utr4).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied)
      verifyUtrs(utr1, utr4).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied) withClue "one permitted one denied"

    }
  }

  private def dropDb() = {
    injector.instanceOf[DeniedUtrsRepo].drop.futureValue shouldBe true withClue "could not drop db collection"
  }

  private lazy val baseUrl = s"http://localhost:$port/tps-payments-backend"
  private lazy val httpClient = app.injector.instanceOf[HttpClient]

  private def uploadDeniedUtrs(deniedUtrsCsv: String) = {
    implicit val dummyHc = HeaderCarrier()
    val url = baseUrl + "/upload-denied-utrs"
    httpClient.POSTString[UploadDeniedUtrsResponse](url, deniedUtrsCsv)
  }

  private def verifyUtrs(utrs: Utr*): Future[VerifyUtrResponse] = {
    val utrsSet = utrs.toSet
    implicit val dummyHc = HeaderCarrier()
    val url = baseUrl + "/verify-utrs"
    val request = VerifyUtrsRequest(utrsSet)
    httpClient.POST[VerifyUtrsRequest, VerifyUtrResponse](url, request)
  }

}
