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

import akka.actor.{Actor, ActorRef, Props}
import model.Utr.{AllGood, Denied, Utr, Utrs}

object UtrCacheActor {
  def props(): Props = Props(new UtrCacheActor())

  case class VerifyUtrTo(utr: Utr, replyTo: ActorRef)
  case class RefreshCache(utrs: Utrs)
}
@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Recursion"))
class UtrCacheActor() extends Actor {
  import UtrCacheActor._

  def receive: Receive = handleVerifications(cache = Utrs(Set.empty[Utr]))

  def handleVerifications(cache: Utrs): Receive = {
    case VerifyUtrTo(utr: Utr, replyTo) =>
      val verifyResult = if (cache.utrs.contains(utr)) Denied else AllGood
      replyTo ! verifyResult
    case RefreshCache(utrs) =>
      context.become(handleVerifications(utrs))
  }

}
