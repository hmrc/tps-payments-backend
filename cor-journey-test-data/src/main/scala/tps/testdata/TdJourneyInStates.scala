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

import play.api.libs.json.JsObject
import tps.journey.model.{Journey, JourneyId, JourneyState}
import tps.model._
import tps.pcipalmodel.{ChargeRefNotificationPcipalRequest, PcipalSessionLaunchRequest, PcipalSessionLaunchResponse}
import tps.testdata.util.JsonSyntax.toJsonOps
import tps.testdata.util.ResourceReader

import java.time.Instant

final case class JourneyJson(resourcePath: String) {
  val simpleName: String = resourcePath
    .replace("""/tps/testdata/""", "")
    .replace(""".json""", "")

  lazy val json: JsObject = ResourceReader.read(resourcePath).asJson
}

trait TdJourneyInStates {
  def journeyId: JourneyId
  def pid: String
  def created: Instant
  def navigation: Navigation
  def selectedTaxType: TaxType

  def amountString: String
  final def amount: BigDecimal = BigDecimal(amountString)

  //TODO: provide a strong type for that, use it in Journey, etc. Make sure you don't break existing json formats
  def taxReference: String

  def pcipalSessionLaunchRequest: PcipalSessionLaunchRequest
  def pcipalSessionLaunchResponse: PcipalSessionLaunchResponse
  def pcipalData: ChargeRefNotificationPcipalRequest
  def paymentItemBeforePcipal: PaymentItem // i.e. pcipal data is None
  def paymentItem: PaymentItem
  def paymentSpecificData: PaymentSpecificData

  lazy val journeyCreated: Journey = Journey(
    _id                         = journeyId,
    journeyState                = JourneyState.Landing,
    pid                         = pid,
    created                     = created,
    payments                    = Nil,
    navigation                  = navigation,
    pcipalSessionLaunchRequest  = None,
    pcipalSessionLaunchResponse = None
  )

  def journeyCreatedJson: JourneyJson

  lazy val journeySelectedTaxType: Journey =
    journeyCreated.copy(
      journeyState = JourneyState.EnterPayment(taxType = selectedTaxType)
    )

  def journeySelectedTaxTypeJson: JourneyJson

  lazy val journeyEnteredPayment: Journey =
    journeySelectedTaxType.copy(
      journeyState = JourneyState.BasketNotEmpty,
      payments     = List(paymentItemBeforePcipal)
    )

  def journeyEnteredPaymentJson: JourneyJson

  lazy val journeyAtPciPal: Journey =
    journeyEnteredPayment.copy(
      journeyState                = JourneyState.AtPciPal,
      pcipalSessionLaunchRequest  = Some(pcipalSessionLaunchRequest),
      pcipalSessionLaunchResponse = Some(pcipalSessionLaunchResponse)
    )

  def journeyAtPciPalJson: JourneyJson

  lazy val journeyResetByPciPal: Journey = journeyAtPciPal.copy(
    journeyState = JourneyState.ResetByPciPal
  )

  def journeyResetByPciPalJson: JourneyJson

  lazy val journeyFinishedByPciPal: Journey = journeyAtPciPal.copy(
    journeyState = JourneyState.FinishedByPciPal
  )

  def journeyFinishedByPciPalJson: JourneyJson

  lazy val journeyBackByPciPal: Journey = journeyAtPciPal.copy(
    journeyState = JourneyState.BackByPciPal
  )

  def journeyBackByPciPalJson: JourneyJson

  lazy val journeyReceivedNotification: Journey = journeyFinishedByPciPal.copy(
    journeyState = JourneyState.ReceivedNotification,
    payments     = List(paymentItem.copy(
      pcipalData = Some(pcipalData)
    ))
  )

  def journeyReceivedNotificationJson: JourneyJson

  lazy val allJourneys: List[(Journey, JourneyJson)] = List(
    (journeyCreated, journeyCreatedJson),
    (journeySelectedTaxType, journeySelectedTaxTypeJson),
    (journeyEnteredPayment, journeyEnteredPaymentJson),
    (journeyAtPciPal, journeyAtPciPalJson),
    (journeyResetByPciPal, journeyResetByPciPalJson),
    (journeyFinishedByPciPal, journeyFinishedByPciPalJson),
    (journeyBackByPciPal, journeyBackByPciPalJson),
    (journeyReceivedNotification, journeyReceivedNotificationJson)
  )
}
