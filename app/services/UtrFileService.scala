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

package services

import model.Utr.{DecryptedUtrFile, EncryptedUtrFile, UtrFileId, Utrs}
import play.api.Logger
import repository.UtrRepo
import util.{Crypto, CsvParser}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UtrFileService @Inject() (
    utrRepo:   UtrRepo,
    crypto:    Crypto,
    csvParser: CsvParser)(implicit ec: ExecutionContext) {
  //TODO Error handling
  def insertUtrFile(decryptedUtrFile: DecryptedUtrFile) = {
    logger.info(s"UtrFileService->insertUtrFile  data:${crypto.encrypt(decryptedUtrFile.decryptedFileContents)}")
    utrRepo.upsertFile(EncryptedUtrFile(crypto.encrypt(decryptedUtrFile.decryptedFileContents)))
  }

  def removeObsoleteFiles: Future[Unit] = {
    logger.info(s"UtrFileService->removeObsoleteFiles ")
    for {
      maybeLatestUtrFileId <- utrRepo.getLatestUtrFileId
      allUtrFileIdList <- utrRepo.getAllUtrFileIds
    } yield {
      maybeLatestUtrFileId match {
        case Some(latestUtrFileId) =>
          allUtrFileIdList
            .filter(_ != latestUtrFileId)
            .foreach(utrRepo.removeUrtFile)
        case None =>
      }
    }
  }

  def getLatestFile = {
    logger.info(s"UtrFileService.getLatestFile ")
    utrRepo.getLatestUtrFileId.flatMap {
      case Some(latestUtrFileId) => getUtrFileById(latestUtrFileId)
      case None =>
        logger.error("Failed to retrieve UTR file from database")
        throw new RuntimeException(s"Failed to retrieve UTR file from database")
    }
  }

  def getUtrFileById(utrFileId: UtrFileId): Future[DecryptedUtrFile] = {
    utrRepo
      .findUtrFile(utrFileId)
      .map(_.flatMap(x => crypto.decrypt(x.encryptedFileContents).toOption)
        .map(DecryptedUtrFile(_))
        .getOrElse(throw new RuntimeException(s"Failed to retrieve file for date: ${utrFileId.value}")))
  }

  def parseDecryptedUtrFile(decryptedUtrFile: DecryptedUtrFile): Future[Utrs] = {
    Future.successful(Utrs(csvParser.parse(decryptedUtrFile.decryptedFileContents).toSet))
  }

  val logger: Logger = Logger(this.getClass)
}
