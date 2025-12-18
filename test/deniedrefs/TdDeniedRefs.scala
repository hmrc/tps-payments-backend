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

package deniedrefs

import deniedrefs.model.{DeniedRefs, DeniedRefsId}
import tps.deniedrefs.model.VerifyRefsRequest
import tps.model.Reference

import java.time.LocalDateTime

object TdDeniedRefs {

  val ref1: Reference = Reference("REF1")
  val ref2: Reference = Reference("REF2")
  val ref3: Reference = Reference("REF3")
  val ref4: Reference = Reference("REF4")
  val ref5: Reference = Reference("REF5")

  val csvFile1: String =
    s"""${ref1.value}
       |${ref2.value}
       |${ref3.value}
       |""".stripMargin

  val csvFile2: String =
    s"""${ref2.value}
       |${ref3.value}
       |${ref4.value}
       |${ref5.value}
       |""".stripMargin

  val deniedRefs1: DeniedRefs = DeniedRefs(
    _id = DeniedRefsId("denied-refs-id-123"),
    refs = List(ref1, ref2, ref3),
    inserted = LocalDateTime.parse("2022-02-04T10:00:24.371")
  )

  val deniedRefs2: DeniedRefs = DeniedRefs(
    _id = DeniedRefsId("denied-refs-id-123"),
    refs = List(ref2, ref3, ref4),
    inserted = LocalDateTime.parse("2022-02-05T10:00:24.371")
  )

  val verifyRefRequest: VerifyRefsRequest = VerifyRefsRequest(Set(ref1))

}
