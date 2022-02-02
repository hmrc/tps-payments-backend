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

package actors

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.pattern.pipe
import model.Utr.{MissingFile, Utr, UtrFileId, Utrs}
import play.api.Logger
import services.UtrFileService

import scala.language.postfixOps

object UtrCacheCommandActor {
  def props(utrCacheActor: ActorRef, utrFileService: UtrFileService): Props = Props(new UtrCacheCommandActor(utrCacheActor, utrFileService))

  trait Command
  case object LoadLatestUtrFile extends Command
  case class VerifyUtr(utr: Utr) extends Command
  case class UtrFileIdOption(maybeUtrFileId: Option[UtrFileId])
  case class UtrsOption(maybeUtrs: Option[Utrs])

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
class UtrCacheCommandActor(utrCacheActor: ActorRef, utrFileService: UtrFileService) extends Actor
  with Stash {
  import UtrCacheCommandActor._
  import UtrCacheActor._

  implicit val dispatcher = context.dispatcher

  override def preStart(): Unit = {
    super.preStart()
  }

  def receive: Receive = handleVerifyUtr(UtrFileIdOption(None))

  def handleVerifyUtr(utrFileIdOption: UtrFileIdOption): Receive = {
    case verifyUtr: VerifyUtr =>
      val replyTo = sender()
      val _ = utrFileService.findLatestUtrFileId.map(UtrFileIdOption) pipeTo self
      context.become(handleUtrFileId(utrFileIdOption, verifyUtr, replyTo))

    case x => logger.warn(s"UtrCacheCommandActor Actor received unsupported command ${x} in handleVerifyUtr state")
  }

  def handleUtrFileId(utrFileIdOption: UtrFileIdOption, verifyUtr: VerifyUtr, replyTo: ActorRef): Receive = {
    case UtrFileIdOption(Some(latestUtrFileId)) if (utrFileIdOption.maybeUtrFileId.fold(false)(cacheUtrFileId => cacheUtrFileId == latestUtrFileId)) =>
      utrCacheActor ! VerifyUtrTo(verifyUtr.utr, replyTo)
      unstashAll()
      context.become(handleVerifyUtr(UtrFileIdOption(Some(latestUtrFileId))))

    case UtrFileIdOption(Some(latestUtrFileId)) =>
      val _ = utrFileService.findUtrsByFileId(latestUtrFileId).map(UtrsOption) pipeTo self
      context.become(handleUtrs(UtrFileIdOption(Some(latestUtrFileId)), verifyUtr, replyTo))

    case UtrFileIdOption(None) =>
      logger.warn("Missing UTR fileId in database")
      replyTo ! MissingFile
      unstashAll()
      context.become(handleVerifyUtr(utrFileIdOption))

    case verifyUtr: VerifyUtr =>
      logger.info(s"Stashing ${verifyUtr} message in handleUtrFileId state ")
      stash()
    case x => logger.warn(s"UtrCacheCommandActor Actor received unsupported command ${x} in handleUtrFileId state")
  }

  def handleUtrs(utrFileIdOption: UtrFileIdOption, verifyUtr: VerifyUtr, replyTo: ActorRef): Receive = {
    case UtrsOption(Some(utrs)) =>
      utrCacheActor ! RefreshCache(utrs)
      utrCacheActor ! VerifyUtrTo(verifyUtr.utr, replyTo)
      unstashAll()
      context.become(handleVerifyUtr(utrFileIdOption))

    case UtrsOption(None) =>
      logger.warn("Missing UTR file in database")
      replyTo ! MissingFile
      unstashAll()
      context.become(handleVerifyUtr(utrFileIdOption))

    case verifyUtr: VerifyUtr =>
      logger.info(s"Stashing ${verifyUtr} message in handleUtrs state ")
      stash()
    case x => logger.warn(s"UtrCacheCommandActor Actor received unsupported command ${x} in handleUtrs state")
  }

  private lazy val logger = Logger(UtrCacheCommandActor.getClass)
}
