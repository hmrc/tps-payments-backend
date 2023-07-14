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

import com.typesafe.config.ConfigFactory
import testsupport.ItSpec
import uk.gov.hmrc.crypto.{AesGCMCrypto, Crypted, PlainText}

class CryptoInvestigationSpec extends ItSpec {

  val oldKey = "MmJhcmNsYXlzc2Z0cGRldg=="
  val newKey = "MWJhcmNsYXlzc2Z0cGRldg=="

  "crypto impl old vs new" in {

    val oldAes = new AesGCMCrypto {
      override val encryptionKey: String = oldKey
    }

    val configNew = ConfigFactory.parseString(
      s"""
         |crypto {
         |  key = "$newKey"
         |  previousKeys = ["$oldKey"]
         |}
         |""".stripMargin
    )

    val cryptoNew = new Crypto(configNew)

    val plain = PlainText("sialala")
    val encryptedOld: Crypted = oldAes.encrypt(plain)
    val decryptedOld: PlainText = oldAes.decrypt(encryptedOld)

    decryptedOld shouldBe plain

    cryptoNew.decrypt(encryptedOld.value) shouldBe "sialala"
  }


//  "crypto old vs new" in {
//
//    val configOld = ConfigFactory.parseString(
//      s"""
//        |crypto {
//        |  key = "$oldKey"
//        |}
//        |""".stripMargin
//    )
//    val configNew = ConfigFactory.parseString(
//      s"""
//        |crypto {
//        |  key = "$newKey"
//        |  previousKeys = ["$oldKey"]
//        |}
//        |""".stripMargin
//    )
//
//    val cryptoOld = new Crypto(configOld)
//    val cryptoNew = new Crypto(configNew)
//
//    val decryptedOld = cryptoOld.encrypt("sialala")
//    cryptoNew.decrypt(decryptedOld) shouldBe "sialala"
//
//  }

  "crypto" in {
    //
    //    val crypto = new Crypto("MWJhcmNsYXlzc2Z0cGRldg==")
    //    val plain: String = "sialala"
    //    val encrypted: String = crypto.encrypt(plain)
    //    val encryptedPreviously = "JUts9EVSIC0ZtAJZf+AepBjM5YTBUVRu2ZtFGnLHOLXLZGRn2zTb"
    //    val decrypted: String = crypto.decrypt(encrypted)
    //
    //    plain should not be encrypted
    //    decrypted shouldBe plain
    //
    //    crypto.decrypt(encryptedPreviously) shouldBe plain
  }


}
