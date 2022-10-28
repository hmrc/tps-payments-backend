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

package repository

import model._
import model.pcipal.PcipalSessionId
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.mongo.MongoComponent

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object TpsPaymentsRepo {
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
}

@Singleton
final class TpsPaymentsRepo @Inject() (
    mongoComponent: MongoComponent,
    config:         RepoConfig
)(implicit ec: ExecutionContext)
  extends Repo[TpsId, TpsPayments](
    collectionName = "tps-payments",
    mongoComponent = mongoComponent,
    indexes        = TpsPaymentsRepo.indexes(config.expireMongo.toSeconds),
    extraCodecs    = Seq.empty,
    replaceIndexes = true
  ) {

  def findPayment(tpsId: TpsId): Future[Option[TpsPayments]] = findById(tpsId)

  def findPaymentItem(id: PaymentItemId): Future[Option[TpsPaymentItem]] =
    find("payments.paymentItemId" -> Some(id)).map { payments =>
      val paymentItems = payments.flatMap { payment =>
        payment.payments.filter(_.paymentItemId.contains(id))
      }

      if (paymentItems.size > 1) throw new RuntimeException(s"Multiple payment items with id ${id.value}")
      else paymentItems.headOption
    }

  def getPayment(tpsId: TpsId): Future[TpsPayments] = findById(tpsId).map {
    case Some(tpsPayment) => tpsPayment
    case None             => throw new RuntimeException(s"Record with id ${tpsId.value} not found")
  }

  def findByPcipalSessionId(id: PcipalSessionId): Future[TpsPayments] =
    find("pciPalSessionId" -> id.value).map { payments =>
      if (payments.size > 1)
        throw new RuntimeException(s"Found ${payments.size} records with id ${id.value}")
      else
        payments.headOption.getOrElse(throw new IdNotFoundException(s"Could not find pcipalSessionId: ${id.value}"))
    }

  def removeByReferenceForTest(references: List[String]): Future[Long] =
    remove("payments.paymentSpecificData.ninoPart1" -> Json.obj("$in" -> toJson(references)))

  def findByReferenceForTest(reference: String): Future[List[TpsPayments]] =
    find("payments.paymentSpecificData.ninoPart1" -> reference)

  def surfaceModsDataForRecon(modsReferences: List[String]): Future[List[PaymentSpecificData]] = {
    find("payments.chargeReference" -> Json.obj("$in" -> toJson(modsReferences)))
      .map { listOfPayments =>
        listOfPayments
          .flatMap { tpsPayments =>
            tpsPayments.payments.filter(_.taxType == TaxTypes.MIB)
              .map { tpsPaymentItem =>
                tpsPaymentItem.paymentSpecificData
              }
          }
      }
  }
}
