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
import tps.journey.model.Journey

object TdAll extends TdAll

trait TdAll
  extends AnyRef
  with TdBase
  with TdRequest
  with TdJourneyMib
  with TdJourneyPngr
  with TdJourneyChildBenefit
  with TdJourneyCotax
  with TdJourneySa
  with TdJourneySdlt
  with TdJourneySafe
  with TdJourneyNtc
  with TdJourneyPaye
  with TdJourneyNps
  with TdJourneyVat
  with TdJourneyPpt
  with TdMultiPaymentJourney {

  @SuppressWarnings(Array(
    "org.wartremover.warts.JavaSerializable",
    "org.wartremover.warts.Serializable",
    "org.wartremover.warts.Product"))
  lazy val allTdJourneyInStates: List[TdJourneyInStates] = List(
    TdJourneyChildBenefit,
    TdJourneyCotax,
    TdJourneyMib,
    TdJourneyNps,
    TdJourneyNtc,
    TdJourneyPaye,
    TdJourneyPngr,
    TdJourneyPpt,
    TdJourneySa,
    TdJourneySafe,
    TdJourneySdlt,
    TdJourneyVat
  )

  lazy val allTdJourneysWithJson: List[(Journey, JourneyJson)] =
    allTdJourneyInStates
      .map(_.allJourneys)
      .foldLeft[List[(Journey, JourneyJson)]](Nil)(_ ++ _)
      .++(TdJourneyMultiPayment.allJourneys)

}
