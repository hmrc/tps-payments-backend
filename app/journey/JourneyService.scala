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

import journey.JourneyService.FindByPcipalSessionIdResult
import play.api.Logger
import tps.journey.model.{Journey, JourneyId}
import tps.model._
import tps.pcipalmodel.{ChargeRefNotificationPcipalRequest, PcipalInitialValues, PcipalSessionId}
import tps.utils.SafeEquals.EqualsOps
import util.Crypto

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class JourneyService @Inject() (crypto: Crypto, journeyRepo: JourneyRepo)(implicit ec: ExecutionContext) {

  private lazy val logger = Logger(this.getClass)

  def upsert(journey: Journey): Future[Unit] =
    journeyRepo
      .upsert(encryptOrDecryptSensitiveJourneyFields(journey)(encryptString))
      .map(_ => ())

  def find(journeyId: JourneyId): Future[Option[Journey]] =
    journeyRepo
      .findById(journeyId)
      .map(_.map(encryptOrDecryptSensitiveJourneyFields(_)(decryptString)))

  def findByPcipalSessionId(pcipalSessionId: PcipalSessionId, paymentItemId: PaymentItemId): Future[FindByPcipalSessionIdResult] = {
    journeyRepo
      .findByPcipalSessionId(pcipalSessionId)
      .map {
        case Nil =>
          logger.info(s"No Journey by session id found [pcipalSessionId:${pcipalSessionId.value}] [paymentItemId:${paymentItemId.value}]")
          FindByPcipalSessionIdResult.NoJourneyBySessionId
        case one :: Nil =>
          val journey = encryptOrDecryptSensitiveJourneyFields(one)(decryptString)
          val hasPaymentItem = journey.payments.exists(_.paymentItemId === paymentItemId)
          if (hasPaymentItem) FindByPcipalSessionIdResult.Found(journey)
          else FindByPcipalSessionIdResult.NoMatchingPaymentItem(journey)
        case multiple => throw new RuntimeException(s"Found ${multiple.size.toString} journeys with given pcipalSessionId [${pcipalSessionId.value}]")
      }
  }

  def findPaymentItem(paymentItemId: PaymentItemId): Future[Option[PaymentItem]] = {
    journeyRepo
      .findByPaymentItemId(paymentItemId)
      .map {
        case Nil        => None
        case one :: Nil => Some(encryptOrDecryptSensitiveJourneyFields(one)(decryptString))
        case multiple   => throw new RuntimeException(s"Found ${multiple.size.toString} journeys with given paymentItemId [${paymentItemId.value}]")
      }
      .map(_.map(_.payments.filter(_.paymentItemId === paymentItemId)))
      .map(_.map {
        case Nil        => throw new RuntimeException(s"Expected paymentItem in this journey [paymentItemId:${paymentItemId.value}]")
        case one :: Nil => one
        case multiple   => throw new RuntimeException(s"Found ${multiple.size.toString} paymentItems in one journey [paymentItemId:${paymentItemId.value}]")
      })
  }

  def updateJourneyWithPcipalData(journey: Journey, pcipalData: ChargeRefNotificationPcipalRequest): Journey = {
    val updatedJourney = journey.copy(payments = journey.payments.map {
      case p: PaymentItem if p.paymentItemId === pcipalData.paymentItemId => p.copy(pcipalData = Some(pcipalData))
      case p => p
    })

    if (updatedJourney === journey) {
      logger.error(s"Could not update journey with pciPalData [journeyId: ${journey.id.value}] [paymentItemId:${pcipalData.paymentItemId.value}]")
    } else {
      logger.info(s"Updatedjourney with pciPalData [journeyId: ${journey.id.value}] [paymentItemId:${pcipalData.paymentItemId.value}]")
    }

    updatedJourney
  }

  private val encryptString: String => String = s => crypto.encrypt(s)
  private val decryptString: String => String = s => Try(crypto.decrypt(s)) match {
    case Success(s) => s
    case Failure(e) => throw new RuntimeException(s"Failed to decrypt journey. Has encryption key changed?", e)
  }

  private def encryptOrDecryptSensitiveJourneyFields(journey: Journey)(encryptOrDecrypt: String => String): Journey =
    journey.copy(
      payments                   = journey.payments.map(encryptOrDecryptSensitivePaymentItemFields(_)(encryptOrDecrypt)),
      pcipalSessionLaunchRequest = journey.pcipalSessionLaunchRequest.map { pcipalSessionLaunchRequest =>
        pcipalSessionLaunchRequest.copy(
          InitialValues = pcipalSessionLaunchRequest.InitialValues.map(encryptOrDecryptPcipalInitialValue(_)(encryptOrDecrypt))
        )
      }
    )

  /* If we ever move to scala 3, we can use monocle to do this, it's cleaner code,
   * i.e.
   * paymentItem
   * .focus(_.customerName.value).modify(encryptStringOrDecrypt)
   * .focus(_.email.some.value).modify(encryptStringOrDecrypt)
   * However scala 2.13 version doesn't work with nested options (so email etc is clunky), for now just use copy :(
   */
  private def encryptOrDecryptSensitivePaymentItemFields(paymentItem: PaymentItem)(encryptOrDecrypt: String => String): PaymentItem =
    paymentItem.copy(
      customerName        = CustomerName(encryptOrDecrypt(paymentItem.customerName.value)),
      chargeReference     = encryptOrDecrypt(paymentItem.chargeReference),
      pcipalData          = paymentItem.pcipalData.map(encryptOrDecryptPcipalData(_)(encryptOrDecrypt)),
      paymentSpecificData = encryptOrDecryptPaymentSpecificData(paymentItem.paymentSpecificData)(encryptOrDecrypt),
      email               = paymentItem.email.map(email => Email(encryptOrDecrypt(email.value)))
    )

  private def encryptOrDecryptPaymentSpecificData(paymentSpecificData: PaymentSpecificData)(encryptOrDecrypt: String => String): PaymentSpecificData = {
    paymentSpecificData match {
      case psd @ PngrSpecificData(_, _, _, _)   => psd.copy(chargeReference = encryptOrDecrypt(psd.chargeReference))
      case psd @ MibSpecificData(_, _, _, _)    => psd.copy(chargeReference = encryptOrDecrypt(psd.chargeReference))
      case psd @ ChildBenefitSpecificData(_)    => psd.copy(encryptOrDecrypt(psd.childBenefitYReference))
      case psd @ SaSpecificData(_)              => psd.copy(encryptOrDecrypt(psd.saReference))
      case psd @ SdltSpecificData(_)            => psd.copy(encryptOrDecrypt(psd.sdltReference))
      case psd @ SafeSpecificData(_)            => psd.copy(encryptOrDecrypt(psd.safeReference))
      case psd @ CotaxSpecificData(_)           => psd.copy(encryptOrDecrypt(psd.cotaxReference))
      case psd @ NtcSpecificData(_)             => psd.copy(encryptOrDecrypt(psd.ntcReference))
      case psd @ PayeSpecificData(_, _, _)      => psd.copy(encryptOrDecrypt(psd.payeReference))
      case psd @ NpsSpecificData(_, _, _, _, _) => psd.copy(npsReference = encryptOrDecrypt(psd.npsReference))
      case psd @ VatSpecificData(_, _)          => psd.copy(vatReference = encryptOrDecrypt(psd.vatReference))
      case psd @ PptSpecificData(_)             => psd.copy(pptReference = encryptOrDecrypt(psd.pptReference))
    }
  }

  private def encryptOrDecryptPcipalData(pcipalData: ChargeRefNotificationPcipalRequest)(encryptOrDecrypt: String => String): ChargeRefNotificationPcipalRequest =
    pcipalData.copy(
      TaxReference    = encryptOrDecrypt(pcipalData.TaxReference),
      ChargeReference = encryptOrDecrypt(pcipalData.ChargeReference)
    )

  private def encryptOrDecryptPcipalInitialValue(pcipalInitialValue: PcipalInitialValues)(encryptOrDecrypt: String => String): PcipalInitialValues =
    pcipalInitialValue.copy(
      UTRReference    = encryptOrDecrypt(pcipalInitialValue.UTRReference),
      name1           = encryptOrDecrypt(pcipalInitialValue.name1),
      chargeReference = encryptOrDecrypt(pcipalInitialValue.chargeReference)
    )

}

object JourneyService {

  sealed trait FindByPcipalSessionIdResult
  object FindByPcipalSessionIdResult {
    /**
     * Journey Found by PcipalSessionId and payments contain item with give paymentItemId
     */
    final case class Found(journey: Journey) extends FindByPcipalSessionIdResult

    /**
     * No journey with given PcipalSessionId
     */
    case object NoJourneyBySessionId extends FindByPcipalSessionIdResult

    /**
     * Journey Found by PcipalSessionId but there is no PaymentItem in payments with given paymentItemId.
     */
    final case class NoMatchingPaymentItem(journey: Journey) extends FindByPcipalSessionIdResult
  }
}
