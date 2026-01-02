/*
 * Copyright 2025 HM Revenue & Customs
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

package tps.model

import testsupport.UnitSpec

class CustomerNameSpec extends UnitSpec:

  val testCases: Map[String, String] = Map(
    "  COMPANY@# Ltd.  "           -> "COMPANY Ltd",
    "John   Doe!!!"                -> "John Doe",
    "Best-Buy!!! Stores"           -> "BestBuy Stores",
    "123 Trading Co. (UK)"         -> "123 Trading Co UK",
    " O'Reilly * Books "           -> "OReilly Books",
    "A/B Enterprises"              -> "AB Enterprises",
    "Smith, Johnson & Partners"    -> "Smith Johnson Partners",
    "  Global-Tech$$$ Solutions  " -> "GlobalTech Solutions",
    "Foo___Bar   Inc."             -> "FooBar Inc",
    " Alpha  Beta    Gamma "       -> "Alpha Beta Gamma",
    "Tr@d!ng £ Co."                -> "Trdng Co",
    "    Mega###Corp!!!   "        -> "MegaCorp",
    "Jean-Luc   Picard"            -> "JeanLuc Picard",
    "New\tLine\nCompany"           -> "NewLineCompany"
  )

  "forRecon" - {
    for (testValue, expectedResult) <- testCases do
      s"should sanitize the $testValue" in {

        CustomerName(testValue).forRecon.value shouldBe expectedResult
      }
  }
