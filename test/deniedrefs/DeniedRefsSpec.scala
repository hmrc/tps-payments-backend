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

package deniedrefs

import deniedrefs.model._
import support.ItSpec
import support.testdata.TestData._
import tps.model.Reference
import tps.deniedrefsmodel.{VerifyRefsResponse, VerifyRefStatuses, VerifyRefsRequest}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.Future

class DeniedRefsSpec extends ItSpec {

  "upload denied refs" in {
    val uploadResult1: UploadDeniedRefsResponse = uploadDeniedRefs(csvFile1).futureValue
    uploadResult1.size shouldBe 3
    val uploadResult2: UploadDeniedRefsResponse = uploadDeniedRefs(csvFile2).futureValue
    uploadResult2.size shouldBe 4

    uploadResult2.inserted isAfter uploadResult1.inserted shouldBe true withClue "second upload is newer"

  }

  "verify refs" in {
    dropDb() withClue "given emtpy database"

    withClue("there is no information whether an ref is denied") {
      verifyRefs().futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.MissingInformation)
      verifyRefs(ref1, ref2, ref3).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.MissingInformation)
      verifyRefs(ref2).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.MissingInformation)
      verifyRefs(ref3).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.MissingInformation)
      verifyRefs(ref4).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.MissingInformation)
    }

    withClue("after uploading csv with denied refs some refs should be denied") {
      uploadDeniedRefs(csvFile1).futureValue.size shouldBe 3
      verifyRefs().futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.RefPermitted)
      verifyRefs(ref1, ref2, ref3).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.RefDenied)
      verifyRefs(ref2).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.RefDenied)
      verifyRefs(ref3).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.RefDenied)
      verifyRefs(ref4).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.RefPermitted)
      verifyRefs(ref1, ref4).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.RefDenied) withClue "one permitted one denied"
    }

    withClue("after uploading the second csv with denied refs some other refs should be denied") {

      uploadDeniedRefs(csvFile2).futureValue.size shouldBe 4
      verifyRefs(ref1).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.RefPermitted)
      verifyRefs(ref2).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.RefDenied)
      verifyRefs(ref3).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.RefDenied)
      verifyRefs(ref4).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.RefDenied)
      verifyRefs(ref1, ref4).futureValue shouldBe VerifyRefsResponse(VerifyRefStatuses.RefDenied) withClue "one permitted one denied"

    }
  }

  private def dropDb() = {
    injector.instanceOf[DeniedRefsRepo].drop().futureValue shouldBe true withClue "could not drop db collection"
  }

  private lazy val baseUrl = s"http://localhost:${port.toString}/tps-payments-backend"
  private lazy val httpClient = app.injector.instanceOf[HttpClient]

  private def uploadDeniedRefs(deniedRefsCsv: String) = {
    implicit val dummyHc: HeaderCarrier = HeaderCarrier()
    val url = baseUrl + "/upload-denied-refs"
    httpClient.POSTString[UploadDeniedRefsResponse](url, deniedRefsCsv)
  }

  private def verifyRefs(refs: Reference*): Future[VerifyRefsResponse] = {
    val refsSet = refs.toSet
    implicit val dummyHc: HeaderCarrier = HeaderCarrier()
    val url = baseUrl + "/verify-refs"
    val request = VerifyRefsRequest(refsSet)
    httpClient.POST[VerifyRefsRequest, VerifyRefsResponse](url, request)
  }

}
