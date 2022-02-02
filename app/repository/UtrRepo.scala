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

import model.Utr.{EncryptedUtrFile, UtrFileId}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}

import javax.inject.{Inject, Singleton}
import scala.collection.GenTraversableOnce
import scala.concurrent.{ExecutionContext, Future}

@Singleton
final class UtrRepo @Inject() (reactiveMongoComponent: ReactiveMongoComponent)(implicit ec: ExecutionContext)
  extends Repo[EncryptedUtrFile, UtrFileId]("utr-files", reactiveMongoComponent) {

  def findUtrFile(utrFileId: UtrFileId): Future[Option[EncryptedUtrFile]] = findById(utrFileId)

  def getUrtFile(utrFileId: UtrFileId): Future[EncryptedUtrFile] = findById(utrFileId).map {
    case Some(utrFile) => utrFile
    case None          => throw new RuntimeException(s"Utr File with id ${utrFileId.value} not found")
  }

  def upsertFile(data: EncryptedUtrFile): Future[UpdateWriteResult] = upsert(UtrFileId(data.inserted), data)

  def removeUrtFile(utrFileId: UtrFileId): Future[WriteResult] = removeById(utrFileId)

  def removeAllFiles: Future[WriteResult] = removeAll()

  def getLatestUtrFileId: Future[Option[UtrFileId]] = findAll().map(_.map(_.inserted).safeMax.map(UtrFileId(_)))

  def getAllUtrFileIds: Future[List[UtrFileId]] = findAll().map(_.map(_.inserted).map(UtrFileId(_)))

  implicit class SafeMaxSyntax[T, A](t: T)(implicit ev: T => GenTraversableOnce[A], ord: Ordering[A]) {
    def safeMax: Option[A] = {
      val coll = ev(t)
      if (coll.nonEmpty) Some(coll.max) else None
    }
  }

}
