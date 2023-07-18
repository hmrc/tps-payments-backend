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

import deniedrefs.TdDeniedRefs._
import deniedrefs.model._
import play.api.libs.json.JsObject
import play.api.mvc.Request
import testsupport.ItSpec
import tps.deniedrefs.VerifyRefsConnector
import tps.deniedrefs.model.{VerifyRefStatuses, VerifyRefsRequest, VerifyRefsResponse}
import tps.model.Reference
import tps.testdata.TdAll
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.time.{Clock, ZoneId}
import scala.concurrent.Future

class DeniedRefsSpec extends ItSpec {

  def connector: VerifyRefsConnector = app.injector.instanceOf[tps.deniedrefs.VerifyRefsConnector]

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

  "findLatestDeniedRefsIdJson should return only id so this query result doesn't transport unused fields" in {
    val repo = app.injector.instanceOf[DeniedRefsRepo]
    uploadDeniedRefs(csvFile1).futureValue withClue "insert something to db"
    val result: JsObject = repo.findLatestDeniedRefsIdJson().futureValue.value
    result.fields.map(_._1) shouldBe List("_id") withClue "only '_id' in the result"
  }

  // trivial test just to make sure no one accidentally renames refs field name in DeniedRefs case class, without updating projection in DeniedRefsRepo
  "sanity check the DeniedRefs case class" in {
    val fieldNames: Array[String] = classOf[DeniedRefs].getDeclaredFields.map(_.getName)
    fieldNames.contains("refs") shouldBe true withClue s"It looks like the field name: [refs] has changed, make sure you update the projection. Current list of fields: ${fieldNames.mkString("Array(", ", ", ")")}"
  }

  override def clock: Clock = Clock.system(ZoneId.of("Europe/London"))

  private def dropDb() = {
    injector.instanceOf[DeniedRefsRepo].drop().futureValue shouldBe true withClue "could not drop db collection"
  }

  private def uploadDeniedRefs(deniedRefsCsv: String) = {
    implicit val dummyHc: HeaderCarrier = HeaderCarrier()
    val url = s"http://localhost:${port.toString}/tps-payments-backend" + "/upload-denied-refs"
    val httpClient = app.injector.instanceOf[HttpClient]
    httpClient.POSTString[UploadDeniedRefsResponse](url, deniedRefsCsv)
  }

  private def verifyRefs(refs: Reference*): Future[VerifyRefsResponse] = {
    val verifyRefsRequest = VerifyRefsRequest(refs.toSet)
    implicit val request: Request[_] = TdAll.request
    connector.verifyRefs(verifyRefsRequest)
  }

}
