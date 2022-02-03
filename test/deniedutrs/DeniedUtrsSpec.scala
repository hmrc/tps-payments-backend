package deniedutrs

import _root_.model.Utr
import deniedutrs.model._
import support.{ItSpec, TestData}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.Future

class DeniedUtrsSpec extends ItSpec {

  "upload and verify utr scenario" in {
    dropDb() withClue "given emtpy database"

    withClue("there is no information whether an utr is denied") {
      verifyUtr(TestData.utr1).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.MissingInformation)
      verifyUtr(TestData.utr2).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.MissingInformation)
      verifyUtr(TestData.utr3).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.MissingInformation)
      verifyUtr(TestData.utr4).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.MissingInformation)
    }


    withClue("after uploading csv with denied utrs some utrs should be denied") {
      uploadDeniedUtrs(TestData.csvFile1).futureValue.size shouldBe 3

      verifyUtr(TestData.utr1).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied)
      verifyUtr(TestData.utr2).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied)
      verifyUtr(TestData.utr3).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied)
      verifyUtr(TestData.utr4).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrPermitted)
    }

    withClue("after uploading the second csv with denied utrs some other utrs should be denied") {

      uploadDeniedUtrs(TestData.csvFile2).futureValue.size shouldBe 4
      verifyUtr(TestData.utr1).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrPermitted)
      verifyUtr(TestData.utr2).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied)
      verifyUtr(TestData.utr3).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied)
      verifyUtr(TestData.utr4).futureValue shouldBe VerifyUtrResponse(VerifyUtrStatuses.UtrDenied)
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

  private def verifyUtr(utr: Utr): Future[VerifyUtrResponse] = {
    implicit val dummyHc = HeaderCarrier()
    val url = baseUrl + "/verify-utr"
    val request = VerifyUtrRequest(utr)
    httpClient.POST[VerifyUtrRequest, VerifyUtrResponse](url, request)
  }

}
