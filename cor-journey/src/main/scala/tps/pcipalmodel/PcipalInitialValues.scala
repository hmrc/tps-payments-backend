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

package tps.pcipalmodel

import tps.model.{HeadOfDutyIndicator, PaymentItemId}

//TODO: make strong types instead of Strings
final case class PcipalInitialValues(
  clientId:           String,
  pid:                String,
  accountOfficeId:    String,
  HODIdentifier:      HeadOfDutyIndicator,
  UTRReference:       String,
  name1:              String,
  amount:             String,
  taxAmount:          Option[String], // for Paye only
  nicAmount:          Option[String], // for Paye only
  lnpClass2:          Option[String], // for Nps only
  nirRate:            Option[String], // for Nps only
  startDate:          Option[String], // for Nps only
  endDate:            Option[String], // for Nps only
  vatPeriodReference: Option[String], // always None
  vatRemittanceType:  Option[String], // for Vat only
  paymentItemId:      PaymentItemId,
  chargeReference:    String,
  taxRegimeDisplay:   String,
  reference:          String,
  increment:          String
) derives CanEqual

object PcipalInitialValues:

  val ClientID            = "ClientID_"
  val PID                 = "PID_"
  val AccountOfficeID     = "AccountOfficeID_"
  val HODIdentifier       = "HODIdentifier_"
  val UTRReference        = "UTRReference_"
  val Name1               = "Name1_"
  val Amount              = "Amount_"
  val TaxAmount           = "TaxAmount_"
  val NICAmount           = "NICAmount_"
  val LNPClass2           = "LNPClass2_"
  val NIRSRate            = "NIRSRate_"
  val StartDate           = "StartDate_"
  val EndDate             = "EndDate_"
  val VATPeriodReference  = "VATPeriodReference_"
  val VATRemittanceType   = "VATRemittanceType_"
  val PaymentItemID       = "PaymentItemID_"
  val ChargeReference     = "ChargeReference_"
  val TaxRegimeDisplay    = "TaxRegimeDisplay_"
  val ReferenceDisplay    = "Reference_"
  val UTRBlacklistFlag    = "UTRBlacklistFlag"
  val postcodeFlag        = "postcodeFlag"
  val taxRegime           = "taxRegime"
  val TotalTaxAmountToPay = "TotalTaxAmountToPay"
  val callbackUrl         = "callbackUrl"
  val backURL             = "backURL"
  val resetURL            = "resetURL"
  val finishURL           = "finishURL"
  val LanguageFlag        = "LanguageFlag"
