/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import model.pcipal.PcipalSessionId
import model.{TpsId, TpsPayments}
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes._
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

@Singleton
final class TpsRepo @Inject() (reactiveMongoComponent: ReactiveMongoComponent, config: RepoConfig)(implicit ec: ExecutionContext)
  extends Repo[TpsPayments, TpsId]("tps-payments", reactiveMongoComponent) {

  override def indexes: Seq[Index] = Seq(
    Index(
      key     = Seq("created" -> IndexType.Ascending),
      name    = Some("createdIdx"),
      options = BSONDocument("expireAfterSeconds" -> config.expireMongo.toSeconds)
    ),
    Index(
      key  = Seq("pciPalSessionId" -> IndexType.Ascending),
      name = Some("pciPalSessionId")
    )
  )

  def findPayment(tpsId: TpsId): Future[Option[TpsPayments]] = findById(tpsId)

  def getPayment(tpsId: TpsId): Future[TpsPayments] = {
    for {
      tpsPaymentsOption <- findById(tpsId)
    } yield tpsPaymentsOption match {
      case Some(tpsPayment) => tpsPayment
      case None             => throw new RuntimeException(s"Record with id ${tpsId.value} not found")
    }
  }

  def findByPcipalSessionId(pcipalSessionId: PcipalSessionId): Future[TpsPayments] = {
    for {
      tpsPayments <- find("pciPalSessionId" -> pcipalSessionId.value)
    } yield {
      if (tpsPayments.nonEmpty && (tpsPayments.size == 1))
        tpsPayments(0)
      else
        throw new RuntimeException(
          if (tpsPayments.nonEmpty) s"Found more than one record with id ${pcipalSessionId.value}, size was ${tpsPayments.size}"
          else s"Could not find pcipalSessionId: ${pcipalSessionId.value}"
        )

    }

  }

  def removeByReferenceForTest(references: List[String]): Future[WriteResult] = {
    remove("payments.paymentSpecificData.ninoPart1" -> Json.obj("$in" -> Json.toJson(references)))
  }

  def findByReferenceForTest(reference: String): Future[List[TpsPayments]] = {
    find("payments.paymentSpecificData.ninoPart1" -> reference)
  }
}
