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
    utrRepo.upsertFile(EncryptedUtrFile(crypto.encrypt(decryptedUtrFile.decryptedFileContents)))
  }

  def removeObsoleteFiles: Future[Unit] = {
    //    logger.info(s"UtrFileService->removeObsoleteFiles ")
    for {
      latestUtrFileId <- utrRepo.getLatestUtrFileId
      allUtrFileIdList <- utrRepo.getAllUtrFileIds
    } yield {
      allUtrFileIdList
        .filter(_ != latestUtrFileId)
        .foreach(utrRepo.removeUrtFile)
    }
  }

  def getLatestUtrsFromDB = {
    for {
      latestUtrFileId <- utrRepo.getLatestUtrFileId
      decryptedUtrFile <- getUtrFileById(latestUtrFileId)
      utrs <- parseAndValidateDecryptedUtrFile(decryptedUtrFile)
    } yield (utrs)
  }

  def findUtrsByFileId(utrFileId: UtrFileId) = {
    findUtrFileById(utrFileId).map(_.flatMap{ decryptedUtrFile =>
      parseAndValidateDecryptedUtrFileOption(decryptedUtrFile)
    })
  }

  def getLatestUtrFileId = utrRepo.getLatestUtrFileId

  def findLatestUtrFileId = utrRepo.findLatestUtrFileId

  def parseAndValidateDecryptedUtrFileOption(decryptedUtrFile: DecryptedUtrFile): Option[Utrs] = {
    val utrs = Utrs(csvParser.parse(decryptedUtrFile.decryptedFileContents).toSet)
    if (!validateUtrs(utrs)) None
    else Some(utrs)
  }

  def parseAndValidateDecryptedUtrFile(decryptedUtrFile: DecryptedUtrFile): Future[Utrs] = {
    Future.successful{
      val utrs = Utrs(csvParser.parse(decryptedUtrFile.decryptedFileContents).toSet)
      if (!validateUtrs(utrs)) throw new RuntimeException("File upload failed on validation")
      else utrs
    }
  }

  def findUtrFileById(utrFileId: UtrFileId): Future[Option[DecryptedUtrFile]] = {
    utrRepo
      .findUtrFile(utrFileId)
      .map(_.flatMap(x => crypto.decrypt(x.encryptedFileContents).toOption)
        .map(DecryptedUtrFile(_))
      )
  }

  def getUtrFileById(utrFileId: UtrFileId): Future[DecryptedUtrFile] = {
    utrRepo
      .findUtrFile(utrFileId)
      .map(_.flatMap(x => crypto.decrypt(x.encryptedFileContents).toOption)
        .map(DecryptedUtrFile(_))
        .getOrElse(throw new RuntimeException(s"Failed to retrieve file for date: ${utrFileId.value}")))
  }

  //TODO WG - confirm validation rules for UTR, we need to do same validations as FrontEnd ?? Now just dummy validation
  private def validateUtrs(utrs: Utrs): Boolean = utrs.utrs.forall(_.value.length > 0)

  val logger: Logger = Logger(this.getClass)
}
