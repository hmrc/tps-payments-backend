package actors

import akka.actor.{Actor, ActorRef, Props}
import model.Utr.{Utr, UtrFileId, Utrs}
import play.api.Logger

object UtrCacheActor {
  def props():Props = Props(new UtrCacheActor())

  case class BulkInsertUtrs(utrs:Utrs)
  case object LoadLatestUtrFile
  case class VerifyUtr(utr:Utr)
  case class RefreshCacheWith
}

class UtrCacheActor () extends Actor {
  import UtrCacheActor._

  override def preStart(): Unit = {
    self ! InitializeCache
    super.preStart()
  }

  def receive: Receive =waitForInitialLoad

  def waitForInitialLoad:Receive = {
    case LoadLatestUtrs =>
      logger.info(s"Proxy got TCPSocketConnected and switch to handle messages")
      context.become(handleWSMessage)
    case TCPSocketConnectionFailed =>
      logger.info(s"Proxy got TCPSocketConnectionFailed and stopping")
      context.stop(self)
    case TCPSocketUnexpectedMessage =>
      logger.info(s"Proxy got TCPSocketUnexpectedMessage and stopping")
      context.stop(self)
    case x =>
      logger.info(s"Proxy during opening TCPSocket got unknown message: $x and stopping")
      context.stop(self)
  }

  def cacheReady(cache:Set[String], utrFileId: UtrFileId):Receive = {
    case
  }

  private lazy val logger = Logger(UtrCacheActor.getClass)
}