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
import uk.gov.hmrc.crypto._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class Crypto(encryptionKeyInBase64: String) { self =>

  @Inject
  def this(servicesConfig: ServicesConfig) = this(servicesConfig.getString("crypto.encryption-key"))

  private val aes = new AesGCMCrypto {
    override val encryptionKey: String = self.encryptionKeyInBase64
  }

  def encrypt(s: String): String = aes.encrypt(PlainText(s)).value

  def decrypt(s: String): String = aes.decrypt(Crypted(s)).value

}
