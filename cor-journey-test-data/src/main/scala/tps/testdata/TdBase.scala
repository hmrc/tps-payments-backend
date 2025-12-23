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

import tps.journey.model.JourneyId
import tps.model.{CustomerName, Email, Navigation, PaymentItemId}
import tps.pcipalmodel.PcipalSessionId

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneOffset}

trait TdBase {

  lazy val pid: String                = "1234567"
  lazy val customerName: CustomerName = CustomerName("customerName")
  //  lazy val chargeReference: String = "chargeReference"
  //  lazy val amountString: String = "100.00"
  //  lazy val amount: BigDecimal = BigDecimal(amountString)
  lazy val email: Email               = Email("test@email.com")
  lazy val flowId: Int                = 1055
  lazy val journeyId: JourneyId       = JourneyId("64886ed616fe8b501cbf0088")

  lazy val navigation: Navigation = Navigation(
    back = "http://localhost:9124/tps-payments/back-from-pcipal",
    reset = "http://localhost:9124/tps-payments/full/reset",
    finish = "http://localhost:9124/tps-payments/finish",
    callback = "http://notification.host/payments/notifications/send-card-payments"
  )

  lazy val dateString: String  = "2059-11-25"
  lazy val timeString: String  = s"${dateString}T16:33:51.880"
  lazy val localDateTime: LocalDateTime =
    // the frozen time has to be in future otherwise the journeys will disappear from mongodb because of expiry index
    LocalDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME)
  lazy val instant: Instant    = localDateTime.toInstant(ZoneOffset.UTC)
  lazy val newInstant: Instant = instant.plusSeconds(20) // used when a new journey is created from existing one

  lazy val pciPalSessionId: PcipalSessionId = PcipalSessionId("dummy-session-id-765955124")

  // TODO: this probably is a transaction number,
  // change it to be a proper transaction number
  // investigate the source of this data
  lazy val pciPalReferenceNumber: String = "3123456701"
  lazy val paymentItemId: PaymentItemId  = PaymentItemId("64897aee16fe8b501cbf008a")

  lazy val cardType: String        = "VISA"
  lazy val cardLast4Digits: String = "1234"
  lazy val linkId: String          = "3097"

}
