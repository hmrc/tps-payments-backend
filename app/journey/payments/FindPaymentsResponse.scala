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

package journey.payments

import journey.payments.FindPaymentsResponse.Payment
import play.api.libs.json.{Json, OFormat}

import java.time.Instant

final case class FindPaymentsResponse(payments: Seq[Payment])

object FindPaymentsResponse:

  final case class Payment(
    reference:            String,
    transactionReference: String,
    amountInPence:        Long,
    createdOn:            Instant,
    taxType:              String
  )

  object Payment:

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    given OFormat[Payment] = Json.format

  given OFormat[FindPaymentsResponse] = Json.format
