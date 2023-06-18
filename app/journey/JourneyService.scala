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

package journey

import play.api.Logger
import repository.JourneyRepo
import tps.journey.model.{Journey, JourneyId}
import tps.model.{PaymentItem, PaymentItemId}
import tps.pcipalmodel.{ChargeRefNotificationPcipalRequest, PcipalSessionId}
import tps.utils.SafeEquals.EqualsOps
import util.Crypto

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class JourneyService @Inject() (
    crypto:      Crypto,
    journeyRepo: JourneyRepo)(implicit ec: ExecutionContext) {

  def upsert(journey: Journey): Future[Unit] = journeyRepo.upsert(encryptEmails(journey)).map(_ => ())

  def find(journeyId: JourneyId): Future[Option[Journey]] =
    journeyRepo
      .findById(journeyId)
      .map(_.map(decryptEmails))

  def findByPcipalSessionId(pcipalSessionId: PcipalSessionId): Future[Option[Journey]] =
    journeyRepo
      .findByPcipalSessionId(pcipalSessionId)
      .map {
        case Nil        => None
        case one :: Nil => Some(decryptEmails(one))
        case multiple   => throw new RuntimeException(s"Found ${multiple.size.toString} journeys with given pcipalSessionId [${pcipalSessionId.value}]")
      }

  def findPaymentItem(paymentItemId: PaymentItemId): Future[Option[PaymentItem]] = {
    journeyRepo
      .findByPaymentItemId(paymentItemId)
      .map {
        case Nil        => None
        case one :: Nil => Some(decryptEmails(one))
        case multiple   => throw new RuntimeException(s"Found ${multiple.size.toString} journeys with given paymentItemId [${paymentItemId.value}]")
      }
      .map(_.map(_.payments.filter(_.paymentItemId === Some(paymentItemId))))
      .map(_.map {
        case Nil        => throw new RuntimeException(s"Expected paymentItem in this journey [paymentItemId:${paymentItemId.value}]")
        case one :: Nil => one
        case multiple   => throw new RuntimeException(s"Found ${multiple.size.toString} paymentItems in one journey [paymentItemId:${paymentItemId.value}]")
      })
  }

  private def encryptEmails(journey: Journey): Journey = {
    val paymentItems: List[PaymentItem] = journey.payments
    val paymentItemsWithEncryptedEmails: List[PaymentItem] = paymentItems.map(tpsPaymentItem => tpsPaymentItem.email match {
      case Some(email) => tpsPaymentItem.copy(email = Some(crypto.encrypt(email)))
      case _           => tpsPaymentItem
    })
    journey.copy(payments = paymentItemsWithEncryptedEmails)
  }

  private def decryptEmails(journey: Journey): Journey = journey.copy(payments = journey
    .payments
    .map(item =>
      item.copy(email = item.email.map(decryptEmail))
    ))

  private def decryptEmail(email: String) = Try(crypto.decrypt(email)) match {
    case Success(v) => v
    case Failure(e) =>
      throw new RuntimeException(s"Failed to decrypt email. Has encryption key changed?", e)
  }

  def updateJourneyWithPcipalData(journey:    Journey,
                                  pcipalData: ChargeRefNotificationPcipalRequest
  ): Journey = {

    val updatedJourney = journey.copy(payments = journey.payments.map {
      case p: PaymentItem if p.paymentItemId === Some(pcipalData.paymentItemId) => p.copy(pcipalData = Some(pcipalData))
      case p => p
    })

    if (updatedJourney === journey) {
      logger.error(s"Could not update journey with pciPalData [journeyId: ${journey.id.value}] [paymentItemId:${pcipalData.paymentItemId.value}]")
    } else {
      logger.info(s"Updatedjourney with pciPalData [journeyId: ${journey.id.value}] [paymentItemId:${pcipalData.paymentItemId.value}]")
    }

    updatedJourney
  }

  private lazy val logger = Logger(this.getClass)
}
