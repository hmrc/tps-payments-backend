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

package util

import testsupport.ItSpec
import testsupport.testdata.TestData.{tpsPaymentsWithEmptyEmail, tpsPaymentsWithoutEmail}
import tps.journey.model.{Journey, JourneyId}
import tps.model.Email

class CryptoSpec extends ItSpec {

  "encrypt/decrypt" in {
    val crypto = app.injector.instanceOf[Crypto]
    val plain: String = "sialala"
    val encrypted: String = crypto.encrypt(plain)
    val decrypted: String = crypto.decrypt(encrypted)

    plain should not be encrypted
    decrypted shouldBe plain

    val e: Exception = intercept[Exception](crypto.decrypt("cant decrypt plain string"))
    e should have message "Unable to decrypt value"
  }
}

class CryptoWithDifferentKeysSpec extends ItSpec {

  /**
   * overwrite the crypto.key value with new one
   * put the old crypto.key field in previous keys
   * note: I used the original crypto.key value to encrypt test@email.com to obtain an encrypted value to insert into mongo.
    */
  override lazy val configOverrides: Map[String, Any] = Map(
    "crypto.key" -> "bWFkZXVwMTIzNDVhYmNkZQ==",
    "crypto.previousKeys.0" -> "MWJhcmNsYXlzc2Z0cGRldg=="
  )

  "successfully decrypt when the key used to encrypt a field is moved to the previousKeys field in config" in {
    val paymentsWithEmailEncrypted = tpsPaymentsWithoutEmail.payments.map(_.copy(email = Some(Email("cvosnBPokl/JvPPXel5xww=="))))
    Option(repo.upsert(tpsPaymentsWithEmptyEmail.copy(payments = paymentsWithEmailEncrypted)).futureValue.getUpsertedId).isDefined shouldBe true
    val crypto = app.injector.instanceOf[Crypto]
    val journey: Option[Journey] = repo.findById(JourneyId("session-48c978bb-64b6-4a00-a1f1-51e267d84f91")).futureValue
    val encryptedEmail: Email =
      journey.map(_.payments.headOption.flatMap(_.email))
        .getOrElse(throw new Exception("somthing went wrong"))
        .getOrElse(throw new Exception("somthing went wrong"))
    crypto.decrypt(encryptedEmail.value) shouldBe "test@email.com"
  }
}
