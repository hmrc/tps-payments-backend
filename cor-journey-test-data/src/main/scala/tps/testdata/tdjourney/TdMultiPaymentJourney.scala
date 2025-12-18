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

package tps.testdata.tdjourney

import tps.journey.model.{Journey, JourneyState}
import tps.pcipalmodel.{PcipalInitialValues, PcipalSessionLaunchRequest, PcipalSessionLaunchResponse}
import tps.testdata.TdBase
import tps.testdata.util.JourneyJson

trait TdMultiPaymentJourney {
  dependencies: TdBase
    with TdJourneyChildBenefit // for 1st item in a basket
    with TdJourneyCotax // for 2nd item in a basket
    with TdJourneyVat // for 3rd item in a basket
    with TdJourneyPaye // for 4th item in a basket
    with TdJourneyNtc  // for 5th item in a basket
  =>

  // special case of the journey data where journey has more then 1 (5 actually) items in a basket
  // TODO: make TdJourneyInStates abstract so it can be used here
  object TdJourneyMultiPayment {

    private lazy val exampleJourneyAfterCreated: Journey = dependencies.TdJourneyChildBenefit.journeyStarted

    lazy val journeyWith5itemsInBasket: Journey         = exampleJourneyAfterCreated.copy(
      payments = List(
        dependencies.TdJourneyChildBenefit.paymentItemEntered,
        dependencies.TdJourneyCotax.paymentItemEntered,
        dependencies.TdJourneyVat.paymentItemEntered,
        dependencies.TdJourneyPaye.paymentItemEntered,
        dependencies.TdJourneyNtc.paymentItemEntered
      )
    )
    lazy val journeyWith5itemsInBasketJson: JourneyJson = JourneyJson(
      "/tps/testdata/multipayment/journey-1-With5itemsInBasket.json"
    )

    private lazy val initialValues: List[PcipalInitialValues] = List(
      dependencies.TdJourneyChildBenefit.pcipalSessionLaunchRequest.InitialValues.headOption
        .getOrElse(throw new RuntimeException("there should be one item in the list")),
      dependencies.TdJourneyCotax.pcipalSessionLaunchRequest.InitialValues.headOption
        .getOrElse(throw new RuntimeException("there should be one item in the list")),
      dependencies.TdJourneyVat.pcipalSessionLaunchRequest.InitialValues.headOption
        .getOrElse(throw new RuntimeException("there should be one item in the list")),
      dependencies.TdJourneyPaye.pcipalSessionLaunchRequest.InitialValues.headOption
        .getOrElse(throw new RuntimeException("there should be one item in the list")),
      dependencies.TdJourneyNtc.pcipalSessionLaunchRequest.InitialValues.headOption.getOrElse(
        throw new RuntimeException("there should be one item in the list")
      )
    ).zipWithIndex.map { t =>
      // update increment!
      val increment: Int = t._2 + 1
      val initialValues  = t._1
      initialValues.copy(increment = increment.toString)
    }

    lazy val amount: BigDecimal =
      dependencies.TdJourneyChildBenefit.amountEdited +
        dependencies.TdJourneyCotax.amountEdited +
        dependencies.TdJourneyVat.amountEdited +
        dependencies.TdJourneyPaye.amountEdited +
        dependencies.TdJourneyNtc.amountEdited

    lazy val pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId = dependencies.flowId,
      InitialValues = initialValues,
      UTRBlacklistFlag = "N",
      postcodeFlag = "Y",
      taxRegime = "gen",
      TotalTaxAmountToPay = amount.toString(),
      callbackUrl = navigation.callback,
      backUrl = navigation.back,
      resetUrl = navigation.reset,
      finishUrl = navigation.finish,
      LanguageFlag = "E"
    )

    lazy val pcipalSessionLaunchResponse: PcipalSessionLaunchResponse = PcipalSessionLaunchResponse(
      Id = dependencies.pciPalSessionId,
      LinkId = dependencies.linkId
    )

    lazy val journeyAtPciPal: Journey =
      journeyWith5itemsInBasket.copy(
        journeyState = JourneyState.AtPciPal,
        pcipalSessionLaunchRequest = Some(pcipalSessionLaunchRequest),
        pcipalSessionLaunchResponse = Some(pcipalSessionLaunchResponse)
      )

    lazy val journeyAtPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/multipayment/journey-2-AtPciPal.json"
    )

    lazy val journeyReceivedAllNotifications: Journey = journeyAtPciPal.copy(
      journeyState = JourneyState.ReceivedNotification,
      payments = List(
        dependencies.TdJourneyChildBenefit.journeyReceivedNotification.payments.headOption
          .getOrElse(throw new RuntimeException("there should be one item in the list")),
        dependencies.TdJourneyCotax.journeyReceivedNotification.payments.headOption
          .getOrElse(throw new RuntimeException("there should be one item in the list")),
        dependencies.TdJourneyVat.journeyReceivedNotification.payments.headOption
          .getOrElse(throw new RuntimeException("there should be one item in the list")),
        dependencies.TdJourneyPaye.journeyReceivedNotification.payments.headOption
          .getOrElse(throw new RuntimeException("there should be one item in the list")),
        dependencies.TdJourneyNtc.journeyReceivedNotification.payments.headOption
          .getOrElse(throw new RuntimeException("there should be one item in the list"))
      )
    )

    lazy val journeyReceivedAllNotificationsJson: JourneyJson = JourneyJson(
      "/tps/testdata/multipayment/journey-3-ReceivedAllNotifications.json"
    )

    lazy val allJourneys: List[(Journey, JourneyJson)] = List(
      (journeyWith5itemsInBasket, journeyWith5itemsInBasketJson),
      (journeyAtPciPal, journeyAtPciPalJson),
      (journeyReceivedAllNotifications, journeyReceivedAllNotificationsJson)
    )

  }

}
