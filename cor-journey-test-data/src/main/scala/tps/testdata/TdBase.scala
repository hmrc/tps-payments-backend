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

package tps.testdata

import tps.model.{JourneyId, Navigation, PaymentItemId}
import tps.pcipalmodel.PcipalSessionId

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneOffset}

trait TdBase {

  def pid: String = "1234567"
  def customerName: String = "customerName"
  //  def chargeReference: String = "chargeReference"
  //  def amountString: String = "100.00"
  //  def amount: BigDecimal = BigDecimal(amountString)
  def email: String = "test@email.com"
  def emailEncrypted: String = "BEru9SQBlqfw0JgiAEKzUXm3zcq6eZHxYFdtl6Pw696S2y+d2gONPeX3MUFcLA=="
  def flowId: Int = 1055
  def journeyId: JourneyId = JourneyId("64886ed616fe8b501cbf0088")

  def navigation: Navigation = Navigation(
    back     = "http://localhost:9124/tps-payments/back-from-pcipal",
    reset    = "http://localhost:9124/tps-payments/full/reset",
    finish   = "http://localhost:9124/tps-payments/finish",
    callback = "http://notification.host/payments/notifications/send-card-payments"
  )

  def dateString: String = "2059-11-25"
  def timeString: String = s"${dateString}T16:33:51.880"
  def localDateTime: LocalDateTime = {
    //the frozen time has to be in future otherwise the journeys will disappear from mongodb because of expiry index
    LocalDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME)
  }
  def instant: Instant = localDateTime.toInstant(ZoneOffset.UTC)
  def newInstant: Instant = instant.plusSeconds(20) //used when a new journey is created from existing one

  def pciPalSessionId: PcipalSessionId = PcipalSessionId("dummy-session-id-765955124")

  //TODO: this probably is a transaction number,
  // change it to be a proper transaction number
  //investigate the source of this data
  def pciPalReferenceNumber: String = "3000000001"
  def paymentItemId: PaymentItemId = PaymentItemId("64897aee16fe8b501cbf008a")

  def cardType: String = "VISA"
  def cardLast4Digits: String = "1234"
  def linkId: String = "3097"

}
