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

import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import play.api.libs.json.Json.toJson
import play.api.libs.json.{Format, Json, OFormat, Reads}
import repository.{Repo, RepoConfig}
import tps.journey.model.{Journey, JourneyId}
import tps.model.{PaymentItemId, PaymentSpecificData, TaxTypes}
import tps.pcipalmodel.PcipalSessionId
import tps.utils.SafeEquals.EqualsOps
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object JourneyRepo {

  def indexes(cacheTtlInSeconds: Long): Seq[IndexModel] = Seq(
    IndexModel(
      keys         = Indexes.ascending("created"),
      indexOptions = IndexOptions().expireAfter(cacheTtlInSeconds, TimeUnit.SECONDS).name("createdIdx")
    ),
    IndexModel(
      keys         = Indexes.ascending("pciPalSessionId"),
      indexOptions = IndexOptions().name("pciPalSessionId")
    )
  )

  /**
   * This format stores date time in mongo specific way.
   * For example: {{{
   *   "\$date":{"\$numberLong":"2837003631880"}
   * }}}
   * Don't change it.
   * Use https://www.epochconverter.com/ to quickly decode Long to Instant.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val formatMongo: OFormat[Journey] = {

    //before OPS-9461 "created" was stored as string and in java.time.LocalDateTime format
    //TODO: Delete this legacy reads in 2024
    val legacyCreatedReads: Reads[Instant] = Reads.of[String].map(s => LocalDateTime.parse(s) toInstant (ZoneOffset.UTC))

    implicit val instantFormatSupportingLegacyReads: Format[Instant] = Format(
      MongoJavatimeFormats.instantReads.orElse(legacyCreatedReads),
      MongoJavatimeFormats.instantWrites
    )
    Json.format[Journey]
  }
}

@Singleton
final class JourneyRepo @Inject() (
    mongoComponent: MongoComponent,
    config:         RepoConfig
)(implicit ec: ExecutionContext)
  extends Repo[JourneyId, Journey](
    collectionName = "tps-payments", //TODO: at some point address the name of the collection. Rename it to journey, rename existing collection to journey
    mongoComponent = mongoComponent,
    indexes        = JourneyRepo.indexes(config.expireMongo.toSeconds),
    extraCodecs    = Seq.empty,
    replaceIndexes = true)(
    manifest         = implicitly[Manifest[Journey]],
    domainFormat     = JourneyRepo.formatMongo,
    executionContext = implicitly[ExecutionContext]) {

  //TODO:x there is missing index on that attribute ,each search results in a full scan which leads to poor performance and DB resources leak
  def findByPaymentItemId(id: PaymentItemId): Future[List[Journey]] =
    find("payments.paymentItemId" -> id)

  def getPayment(journeyId: JourneyId): Future[Journey] = findById(journeyId).map {
    case Some(tpsPayment) => tpsPayment
    case None             => throw new RuntimeException(s"Record with id ${journeyId.value} not found")
  }

  //TODO:x there is missing index on that attribute, each search results in full scan...
  def findByPcipalSessionId(id: PcipalSessionId): Future[List[Journey]] =
    find("pcipalSessionLaunchResponse.Id" -> id.value)

  def removeByReferenceForTest(references: List[String]): Future[Long] =
    remove("payments.paymentSpecificData.ninoPart1" -> Json.obj("$in" -> toJson(references)))

  def findByReferenceForTest(reference: String): Future[List[Journey]] =
    find("payments.paymentSpecificData.ninoPart1" -> reference)

  //TODO:x there is missing index on that attribute, each search results in full scan...
  def surfaceModsDataForRecon(modsReferences: List[String]): Future[List[PaymentSpecificData]] = {
    find("payments.chargeReference" -> Json.obj("$in" -> toJson(modsReferences)))
      .map { listOfPayments =>
        listOfPayments
          .flatMap { tpsPayments =>
            tpsPayments.payments.filter(_.taxType === TaxTypes.MIB)
              .map { tpsPaymentItem =>
                tpsPaymentItem.paymentSpecificData
              }
          }
      }
  }
}
