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

package tps.startjourneymodel

import play.api.libs.json.{Json, OFormat}
import tps.model._
import tps.pcipalmodel.ChargeRefNotificationPcipalRequest

import java.time.Instant
import scala.Option.empty

//TODO: remove this and use dedicated classes and endpoints for Mib and Pngr
final case class StartJourneyRequestMibOrPngr(
    pid:        String,
    payments:   Seq[SjPaymentItem],
    navigation: Navigation
) {

  def tpsPayments(now: Instant): Journey = {
    val tpsPayments = payments.map { p =>
      PaymentItem(
        paymentItemId       = Some(PaymentItemId.fresh()),
        amount              = p.amount,
        headOfDutyIndicator = HeadOfDutyIndicators.B,
        updated             = now,
        customerName        = p.customerName,
        chargeReference     = p.chargeReference,
        pcipalData          = empty[ChargeRefNotificationPcipalRequest],
        paymentSpecificData = p.paymentSpecificData,
        taxType             = p.taxType,
        email               = p.email
      )
    }.toList

    Journey(
      _id        = JourneyId.fresh(),
      pid        = pid,
      created    = now,
      payments   = tpsPayments,
      navigation = Some(navigation)
    )
  }
}

object StartJourneyRequestMibOrPngr {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[StartJourneyRequestMibOrPngr] = Json.format[StartJourneyRequestMibOrPngr]
}
