/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.crypto.{AesGCMCrypto, _}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.util.{Failure, Success, Try}

@Singleton
final class EmailCrypto(encryptionKeyInBase64: String) {
  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""

  @Inject
  def this(servicesConfig: ServicesConfig) = this(servicesConfig.getString("crypto.encryption-key"))

  private val aes = new AesGCMCrypto { override val encryptionKey: String = encryptionKeyInBase64 }

  private def encrypt(s: String): String = aes.encrypt(PlainText(s)).value

  private def decrypt(s: String): Try[String] = Try(aes.decrypt(Crypted(s)).value)

  def maybeDecryptEmail(maybeEmail: Option[String]): Option[String] = {
    maybeEmail.fold(maybeEmail)(email => Some(decryptEmail(email)))
  }

  def decryptEmail(email: String): String = {
    if (email.isEmpty) email
    else {
      decrypt(email) match {
        case Failure(ex)    => decryptFailureException(ex, "email")
        case Success(value) => value
      }
    }
  }

  def encryptEmailIfNotAlreadyEncrypted(email: String): String = {
    if (isEmailNotAlreadyEncrypted(email)) encrypt(email)
    else email
  }

  def isEmailNotAlreadyEncrypted(email: String): Boolean = {
    if (email.isEmpty) false
    else {
      Try(aes.decrypt(Crypted(email)).value) match {
        case Success(email) =>
          if (email.matches(emailRegex)) false
          else true
        case Failure(_) => true
      }
    }
  }

  private def decryptFailureException(ex: Throwable, field: String) = throw new RuntimeException(s"Failed to decrypt field $field due to exception ${ex.getMessage}")
}
