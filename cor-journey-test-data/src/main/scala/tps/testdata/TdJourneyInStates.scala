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

import tps.model._
import tps.pcipalmodel.{ChargeRefNotificationPcipalRequest, PcipalSessionLaunchRequest, PcipalSessionLaunchResponse}

import java.time.Instant

trait TdJourneyInStates {
  def journeyId: JourneyId
  def pid: String
  def created: Instant
  def navigation: Navigation

  def amountString: String
  final def amount: BigDecimal = BigDecimal(amountString)

  //TODO: provide a strong type for that, use it in Journey, etc. Make sure you don't break existing json formats
  def taxReference: String

  def pcipalSessionLaunchRequest: PcipalSessionLaunchRequest
  def pcipalSessionLaunchResponse: PcipalSessionLaunchResponse
  def pciPalData: ChargeRefNotificationPcipalRequest
  def paymentItem: PaymentItem
  def paymentSpecificData: PaymentSpecificData

  //TODO: this is in one particular (final) state
  def journey: Journey = Journey(
    _id                         = journeyId,
    pid                         = pid,
    created                     = created,
    payments                    = List(paymentItem),
    navigation                  = Some(navigation),
    pcipalSessionLaunchRequest  = Some(pcipalSessionLaunchRequest),
    pcipalSessionLaunchResponse = Some(pcipalSessionLaunchResponse)
  )

}
