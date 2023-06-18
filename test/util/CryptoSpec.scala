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

class CryptoSpec extends ItSpec {

  "encrypt/decrypt" in {
    val crypto = app.injector.instanceOf[Crypto]
    val plain: String = "sialala"
    val encrypted: String = crypto.encrypt(plain)
    val decrypted: String = crypto.decrypt(encrypted)

    plain should not be encrypted
    decrypted shouldBe plain

    val e: Exception = intercept[Exception](crypto.decrypt("wrong string"))
    e should have message "Illegal base64 character 20"
  }

}
