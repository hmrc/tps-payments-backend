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

import akka.Done
import model.Utr.{AllGood, Denied, Utr, Utrs, VerifyUtrStatus}
import play.api.cache._

import scala.concurrent.duration._
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UtrValidatorService @Inject() (
    utrCache: AsyncCacheApi
)(implicit executionContext: ExecutionContext) {

  def bulkInsertUtrs(utrs: Utrs): Future[Unit] = Future.successful(utrs.utrs.foreach(addUtrToCache))

  def verifyUtr(utr: Utr): Future[VerifyUtrStatus] = utrCache
    .get[VerifyUtrStatus](utr.value)
    .map(_.getOrElse(AllGood))

  def addUtrToCache(utr: Utr): Future[Done] = utrCache.set(utr.value, Denied, Duration.Inf)

  def invalidateCache: Future[Done] = utrCache.removeAll()
}
