/*
 * Copyright 2023 HM Revenue & Customs
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

package util

import play.api.Logger
import tps.journey.model.Journey
import tps.model.PaymentItemId
import tps.pcipalmodel.PcipalSessionId

/** Logger implementation that is used by kibana dashboards
  */
object KibanaLogger {

  private val log: Logger = Logger("tps-backend-kibana-logger")

  def debug(
    message:         => String,
    journey:         Option[Journey],
    pcipalSessionId: Option[PcipalSessionId],
    paymentItemId:   Option[PaymentItemId]
  ): Unit =
    logMessage(message, journey, pcipalSessionId, paymentItemId, Debug)

  def info(
    message:         => String,
    journey:         Option[Journey],
    pcipalSessionId: Option[PcipalSessionId],
    paymentItemId:   Option[PaymentItemId]
  ): Unit =
    logMessage(message, journey, pcipalSessionId, paymentItemId, Info)

  def warn(
    message:         => String,
    journey:         Option[Journey],
    pcipalSessionId: Option[PcipalSessionId],
    paymentItemId:   Option[PaymentItemId]
  ): Unit =
    logMessage(message, journey, pcipalSessionId, paymentItemId, Warn)

  def error(
    message:         => String,
    journey:         Option[Journey],
    pcipalSessionId: Option[PcipalSessionId],
    paymentItemId:   Option[PaymentItemId]
  ): Unit =
    logMessage(message, journey, pcipalSessionId, paymentItemId, Error)

  def debug(
    message:         => String,
    ex:              Throwable,
    journey:         Option[Journey],
    pcipalSessionId: Option[PcipalSessionId],
    paymentItemId:   Option[PaymentItemId]
  ): Unit =
    logMessage(message, journey, pcipalSessionId, paymentItemId, ex, Debug)

  def info(
    message:         => String,
    ex:              Throwable,
    journey:         Option[Journey],
    pcipalSessionId: Option[PcipalSessionId],
    paymentItemId:   Option[PaymentItemId]
  ): Unit =
    logMessage(message, journey, pcipalSessionId, paymentItemId, ex, Info)

  def warn(
    message:         => String,
    ex:              Throwable,
    journey:         Option[Journey],
    pcipalSessionId: Option[PcipalSessionId],
    paymentItemId:   Option[PaymentItemId]
  ): Unit =
    logMessage(message, journey, pcipalSessionId, paymentItemId, ex, Warn)

  def error(
    message:         => String,
    ex:              Throwable,
    journey:         Option[Journey],
    pcipalSessionId: Option[PcipalSessionId],
    paymentItemId:   Option[PaymentItemId]
  ): Unit =
    logMessage(message, journey, pcipalSessionId, paymentItemId, ex, Error)

  private def makeRichMessage(
    message:         String,
    journey:         Option[Journey],
    pcipalSessionId: Option[PcipalSessionId],
    paymentItemId:   Option[PaymentItemId]
  ): String =
    s"$message" +
      s"${journey.fold("")(j => s" [journeyId:${j.journeyId.value}")}" +
      s"${pcipalSessionId.fold("")(s => s" [pcipalSessionId:${s.value}")}" +
      s"${paymentItemId.fold("")(s => s" [paymentItemId:${s.value}]")}"

  private sealed trait LogLevel derives CanEqual

  private case object Debug extends LogLevel

  private case object Info extends LogLevel

  private case object Warn extends LogLevel

  private case object Error extends LogLevel

  private def logMessage(
    message:         => String,
    journey:         Option[Journey],
    pcipalSessionId: Option[PcipalSessionId],
    paymentItemId:   Option[PaymentItemId],
    level:           LogLevel
  ): Unit = {
    lazy val richMessage = makeRichMessage(message, journey, pcipalSessionId, paymentItemId)
    level match {
      case Debug => log.debug(richMessage)
      case Info  => log.info(richMessage)
      case Warn  => log.warn(richMessage)
      case Error => log.error(richMessage)
    }
  }

  private def logMessage(
    message:         => String,
    journey:         Option[Journey],
    pcipalSessionId: Option[PcipalSessionId],
    paymentItemId:   Option[PaymentItemId],
    ex:              Throwable,
    level:           LogLevel
  ): Unit = {
    lazy val richMessage = makeRichMessage(message, journey, pcipalSessionId, paymentItemId)
    level match {
      case Debug => log.debug(richMessage, ex)
      case Info  => log.info(richMessage, ex)
      case Warn  => log.warn(richMessage, ex)
      case Error => log.error(richMessage, ex)
    }
  }

}
