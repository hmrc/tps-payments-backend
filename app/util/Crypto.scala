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

import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import uk.gov.hmrc.crypto._

@Singleton
class Crypto @Inject() (config: Config) {
  private val encrypterDecrypter = SymmetricCryptoFactory.aesGcmCryptoFromConfig("crypto", config)
  def encrypt(s: String): String = encrypterDecrypter.encrypt(PlainText(s)).value
  def decrypt(s: String): String = encrypterDecrypter.decrypt(Crypted(s)).value
}
