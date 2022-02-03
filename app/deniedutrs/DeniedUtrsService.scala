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

import akka.Done
import akka.stream.Materializer
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.scaladsl.{FileIO, Keep, Sink}
import akka.util.ByteString
import model._
import _root_.model.Utr
import play.api.Logger
import play.api.libs.json.Json
import util.Crypto

import java.nio.file.{Files, Path}
import java.time.{Clock, LocalDateTime}
import java.util.concurrent.atomic.AtomicReference
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class DeniedUtrsService @Inject() (
    deniedUtrsRepo:        DeniedUtrsRepo,
    crypto:                Crypto,
    deniedUtrsIdGenerator: DeniedUtrsIdGenerator,
    clock:                 Clock)(implicit ec: ExecutionContext, materializer: Materializer) {

  /**
   * Upserts object into mongo. It makes sure utrs are encrypted before storing in mongo.
   */
  def upsert(deniedUtrs: DeniedUtrs) = {
    val deniedUtrsEncryted = deniedUtrs.copy(utrs = deniedUtrs.utrs.map(utr => Utr(crypto.encrypt(utr.value))))
    deniedUtrsRepo.upsert(deniedUtrsEncryted)
  }

  /**
   * Finds the latest `DeniedUtrsId`
   *
   * @return None if a csv file has not been uploaded yet
   *         Some(id) containing id of recently uploaded csv file
   */
  def findLatestDeniedUtrsId(): Future[Option[DeniedUtrsId]] = deniedUtrsRepo.findLatestDeniedUtrsId()

  def getDeniedUtrs(deniedUtrsId: DeniedUtrsId): Future[DeniedUtrs] = {
    deniedUtrsRepo
      .findById(deniedUtrsId)
      .map(_.getOrElse(throw new RuntimeException(s"Could not find DeniedUtrs by given id [$deniedUtrsId]")))
      .map(decryptDeniedUtrs)
  }

  private def decryptDeniedUtrs(encryptedDeniedUtrs: DeniedUtrs): DeniedUtrs = {
    val decryptionResult: List[Try[String]] = encryptedDeniedUtrs
      .utrs
      .map(utr => crypto.decrypt(utr.value))
    val successfullyDecrypted: List[Utr] = decryptionResult.collect { case Success(utr) => Utr(utr) }
    decryptionResult.collect {
      case Failure(ex) => logger.error(s"Failed to decrypt utr. Has encryption key changed? [${encryptedDeniedUtrs._id}] [inserted:${encryptedDeniedUtrs.inserted}]", ex)
    }
    encryptedDeniedUtrs.copy(utrs = successfullyDecrypted)
  }

  /**
   * Given a file containing list of denied utrs at `pathToDeniedUtrs` this method:
   * - parses it as CSV
   * - unifies Utrs (trims, adds missing K, uppercases)
   * - deletes file when done
   */
  def parseDeniedUtrs(pathToDeniedUtrs: Path): Future[DeniedUtrs] = {
    val deniedUtrs = FileIO.fromPath(pathToDeniedUtrs)
      .mapMaterializedValue(_ => Done)
      .via(CsvParsing.lineScanner())
      .map(_.map(_.decodeString(ByteString.UTF_8)))
      .map(_.headOption)
      .collect { case Some(utr) => Utr(utr) }
      .map(Utr.canonicalizeUtr)
      .toMat(Sink.collection[Utr, List[Utr]])(Keep.right[Done.type, Future[List[Utr]]])
      .mapMaterializedValue(encryptedUtrsF => encryptedUtrsF
        .map(encryptedUtrs =>
          DeniedUtrs(
            _id      = deniedUtrsIdGenerator.nextId(),
            utrs     = encryptedUtrs,
            inserted = LocalDateTime.now(clock))
        )
      )
      .run()
    deniedUtrs.onComplete(_ => deleteTempFile(pathToDeniedUtrs))
    deniedUtrs
  }

  private def deleteTempFile(pathToCsv: Path): Unit = Future(Files.deleteIfExists(pathToCsv))
    .onComplete {
      case Success(deleted) =>
        if (deleted) logger.info(s"Deleted temporary csv file with utrs [$pathToCsv]")
        else logger.warn(s"Could not deleted temporary csv file with utrs [$pathToCsv]")
      case Failure(ex) => logger.warn(s"Could not deleted temporary csv file with utrs [$pathToCsv]", ex)
    }

  private val cachedDeniedUtrs = new AtomicReference[Option[DeniedUtrs]](None)

  def verifyUtr(utr: Utr): VerifyUtrStatus = {
    cachedDeniedUtrs.get() match {
      case Some(cache) =>
        if (cache.containsUtr(utr))
          VerifyUtrStatuses.UtrDenied
        else
          VerifyUtrStatuses.UtrPermitted
      case None => VerifyUtrStatuses.MissingInformation
    }
  }

  def updateCacheIfNeeded(): Future[Unit] = {
    val cache: Option[DeniedUtrs] = cachedDeniedUtrs.get()
    for {
      latestId: Option[DeniedUtrsId] <- findLatestDeniedUtrsId()
      _ <- (cache, latestId) match {
        case (_, None) =>
          logger.info(s"DeniedUtrs cache is empty. Missing DeniedUtrs in database.")
          Future.successful(())
        case (None, Some(latestId)) =>
          logger.info(s"DeniedUtrs cache is empty. Populating it ... [$latestId]")
          updateCache(latestId)
        case (Some(cached), Some(latestId)) if cached._id == latestId =>
          logger.debug(s"DeniedUtrs cache is up to date [inserted:${cached.inserted}] [$latestId]")
          Future.successful(())
        case (Some(cached), Some(latestId)) if cached._id != latestId =>
          logger.info(s"DeniedUtrs cache is invalid. Populating it ... [$latestId]")
          updateCache(latestId)
      }
    } yield ()
  }

  private def updateCache(latestId: DeniedUtrsId): Future[Unit] = getDeniedUtrs(latestId).map { deniedUtrs =>
    cachedDeniedUtrs.set(Some(deniedUtrs))
    logger.info(s"DeniedUtrs cache updated [size:${deniedUtrs.utrs.size}] [inserted:${deniedUtrs.inserted}] [$latestId]")
  }

  lazy val logger: Logger = Logger(this.getClass)
}
