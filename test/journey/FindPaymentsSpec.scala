/*
 * Copyright 2025 HM Revenue & Customs
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

import journey.FindPaymentsSpec.{PaymentData, PcipalRequestData}
import journey.payments.{FindPaymentsRequest, FindPaymentsResponse}
import org.apache.pekko.stream.Materializer
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testsupport.ItSpec
import tps.journey.model.{Journey, JourneyId, JourneyState}
import tps.model._
import tps.pcipalmodel.{ChargeRefNotificationPcipalRequest, PcipalSessionId, StatusType, StatusTypes}
import tps.testdata.TdAll

import java.time.{Instant, LocalDateTime, LocalTime, ZoneOffset}
import scala.concurrent.duration._

class FindPaymentsSpec extends ItSpec {

  lazy implicit val mat: Materializer = app.injector.instanceOf[Materializer]

  lazy val controller = app.injector.instanceOf[JourneyController]

  def performAction(request: FindPaymentsRequest) =
    controller.findPayments(
      FakeRequest().withBody(request).withHeaders(CONTENT_TYPE -> "application/json")
    )

  def insertData(journey: Journey): Unit = {
    repo.upsert(journey).futureValue.wasAcknowledged() shouldBe true
    ()
  }

  val taxReference = "taxRef"

  val todayStart = LocalDateTime.of(
    TdAll.localDateTime.toLocalDate,
    LocalTime.MIDNIGHT
  ).toInstant(ZoneOffset.of("Z"))

  val pcipalRequestData = PcipalRequestData(taxReference, "transactionRef", StatusTypes.validated)

  val paymentData = PaymentData(
    BigDecimal(101.23),
    Some(pcipalRequestData),
    TaxTypes.Sa
  )

  val journey = newJourney(
    frozenInstant,
    Seq(paymentData)
  )

  val expectedPayment = FindPaymentsResponse.Payment(
    taxReference,
    "transactionRef",
    10123,
    frozenInstant,
    "Sa"
  )

  "Find payments must" - {

    "return a 415 (UNSUPPORTED MEDIA TYPE) when" - {

      "there is no JSON body in the request" in {
        val result = controller.findPayments(FakeRequest()).run()
        status(result) shouldBe UNSUPPORTED_MEDIA_TYPE
      }

    }

    "return a 400 when" - {

      "the JSON body in the request cannot be parsed" in {
        val result = controller.findPayments(
          FakeRequest().withBody("""{ "a": 1 }""").withHeaders(CONTENT_TYPE -> "application/json")
        ).run()

        status(result) shouldBe BAD_REQUEST
        contentAsString(result) should include("Invalid Json")
      }

      "the request contains a numberOfDays value which is less than zero" in {
        val result = performAction(FindPaymentsRequest(Seq.empty, -1))
        status(result) shouldBe BAD_REQUEST
        contentAsString(result) shouldBe "numberOfDays should be greater than zero"
      }

    }

    "return an error when" - {

      "there is an amount in mongo which cannot be converted exactly into an amount in pence" in {
        insertData(
          newJourney(
            todayStart,
            Seq(
              paymentData.copy(amount = BigDecimal(1.2345))
            )
          )

        )

        an[ArithmeticException] shouldBe thrownBy(
          await(performAction(FindPaymentsRequest(Seq(taxReference), 1)))
        )
      }

    }

    "return the correct response" - {

      "the list of references in the request is empty" in {
        val result = performAction(FindPaymentsRequest(Seq.empty, 0))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(FindPaymentsResponse(Seq.empty))
      }

      "there are no payments with the given references" in {
        insertData(journey)

        val result = performAction(FindPaymentsRequest(Seq("ref"), 0))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(FindPaymentsResponse(Seq.empty))
      }

      "there are payments with the given references but they are older than the number of days given in the request" in {
        insertData(journey.copy(created = todayStart.minusMillis((2.days + 1.millis).toMillis)))

        val result = performAction(FindPaymentsRequest(Seq(taxReference), 2))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(FindPaymentsResponse(Seq.empty))
      }

      "there is a payment which was created on exactly the cutoff date derived from the request" in {
        val created = todayStart.minusMillis(2.day.toMillis)

        insertData(journey.copy(created = created))

        val result = performAction(FindPaymentsRequest(Seq(taxReference), 2))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(FindPaymentsResponse(Seq(expectedPayment.copy(createdOn = created))))
      }

      "there is a payment which was created after the cutoff date derived from the request" in {
        val created = todayStart.minusMillis(1.day.toMillis)

        insertData(journey.copy(created = created))

        val result = performAction(FindPaymentsRequest(Seq(taxReference), 2))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(FindPaymentsResponse(Seq(expectedPayment.copy(createdOn = created))))
      }

      "there are multiple payments for the same reference" in {
        val created = todayStart.minusMillis(1.day.toMillis)

        val (taxReference1, taxReference2) = ("taxReference1", "taxReference2")

        insertData(
          newJourney(
            created,
            Seq(
              // this one should get ignored since there is no pcipalRequest data
              PaymentData(
                BigDecimal(12.34),
                None,
                TaxTypes.ChildBenefitsRepayments
              ),
              // this one should get ignored since the status is not validated
              PaymentData(
                BigDecimal(2.34),
                Some(PcipalRequestData(
                  taxReference1,
                  "transaction1",
                  StatusTypes.failed
                )),
                TaxTypes.ChildBenefitsRepayments
              ),
              PaymentData(
                BigDecimal(3.45),
                Some(PcipalRequestData(
                  taxReference1,
                  "transaction2",
                  StatusTypes.validated
                )),
                TaxTypes.MIB
              ),
              PaymentData(
                BigDecimal(4.5),
                Some(PcipalRequestData(
                  taxReference2,
                  "transaction3",
                  StatusTypes.validated
                )),
                TaxTypes.Nps
              ),
              PaymentData(
                BigDecimal(5.67),
                Some(PcipalRequestData(
                  taxReference2,
                  "transaction4",
                  StatusTypes.validated
                )),
                TaxTypes.Ntc
              )
            )
          )
        )

        val expectedResponse = FindPaymentsResponse(
          Seq(
            FindPaymentsResponse.Payment(
              taxReference1,
              "transaction2",
              345,
              created,
              "MIB"
            ),
            FindPaymentsResponse.Payment(
              taxReference2,
              "transaction3",
              450,
              created,
              "Nps"
            ),
            FindPaymentsResponse.Payment(
              taxReference2,
              "transaction4",
              567,
              created,
              "Ntc"
            )
          )
        )

        val result = performAction(FindPaymentsRequest(Seq(taxReference1, taxReference2), 2))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(expectedResponse)
      }

      val expectedTaxTypeStrings =
        TaxTypes.values.map{ t =>
          val expectedString = t match {
            case TpsNativeTaxTypes.ChildBenefitsRepayments => "ChildBenefitsRepayments"
            case TpsNativeTaxTypes.Sa                      => "Sa"
            case TpsNativeTaxTypes.Sdlt                    => "Sdlt"
            case TpsNativeTaxTypes.Safe                    => "Safe"
            case TpsNativeTaxTypes.Cotax                   => "Cotax"
            case TpsNativeTaxTypes.Ntc                     => "Ntc"
            case TpsNativeTaxTypes.Paye                    => "Paye"
            case TpsNativeTaxTypes.Nps                     => "Nps"
            case TpsNativeTaxTypes.Vat                     => "Vat"
            case TpsNativeTaxTypes.Ppt                     => "Ppt"
            case ExternalTaxTypes.MIB                      => "MIB"
            case ExternalTaxTypes.PNGR                     => "PNGR"
          }
          t -> expectedString
        }

      expectedTaxTypeStrings.foreach{
        case (taxType, expectedTaxTypeString) =>

          s"return '$expectedTaxTypeString' as the tax type for TaxType ${taxType.toString}" in {
            val created = todayStart.minusMillis(1.day.toMillis)

            insertData(
              newJourney(
                created,
                Seq(
                  paymentData.copy(taxType = taxType)
                )
              )

            )

            val result = performAction(FindPaymentsRequest(Seq(taxReference), 2))

            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.toJson(
              FindPaymentsResponse(
                Seq(expectedPayment.copy(createdOn = created, taxType = expectedTaxTypeString))
              )
            )

          }

      }

    }

  }

  def newJourney(
      createdOn:   Instant,
      paymentData: Seq[PaymentData]): Journey = {
    val paymentItems = paymentData.map(p =>
      PaymentItem(
        PaymentItemId("paymentId"),
        p.amount,
        HeadOfDutyIndicators.B,
        Instant.now(),
        CustomerName("name"),
        "chargeRef",
        p.pcipalRequestData.map(d =>
          ChargeRefNotificationPcipalRequest(
            HeadOfDutyIndicators.B,
            d.taxReference,
            p.amount,
            BigDecimal(0),
            "cardType",
            d.status,
            PcipalSessionId("pcipalSessionId"),
            d.transactionReference,
            PaymentItemId("paymentId"),
            "chargeRef",
            "referenceNumber",
            "cardLast4"
          )
        ),
        PptSpecificData("unused"),
        p.taxType,
        None
      )
    )

    Journey(
      JourneyId.fresh(),
      JourneyState.Started,
      "pid",
      createdOn,
      paymentItems.toList,
      Navigation("back", "reset", "finish", "callback")
    )
  }

}

object FindPaymentsSpec {

  final case class PcipalRequestData(taxReference:         String,
                                     transactionReference: String,
                                     status:               StatusType)

  final case class PaymentData(amount: BigDecimal, pcipalRequestData: Option[PcipalRequestData], taxType: TaxType)

}
