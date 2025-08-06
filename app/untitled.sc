import com.typesafe.config.ConfigFactory
import uk.gov.hmrc.crypto.{Crypted, Decrypter, Encrypter, PlainText, SymmetricCryptoFactory}
import util.Crypto

val s = "abc123"
val config =   ConfigFactory.parseString(
  """
    |crypto {
    |  key = "MWJhcmNsYXlzc2Z0cGRldg=="
    |  previousKeys = []
    |}
    |""".stripMargin)

val crypto = new Crypto(config)


crypto.encrypt(s)
crypto.encrypt(s)
crypto.encrypt(s)
val p = crypto.encrypt(s)

val encrypterDecrypter: Encrypter with Decrypter =
  SymmetricCryptoFactory.aesCrypto("MWJhcmNsYXlzc2Z0cGRldg==")

encrypterDecrypter.encrypt(PlainText(s))
encrypterDecrypter.encrypt(PlainText(s))
encrypterDecrypter.encrypt(PlainText(s))
encrypterDecrypter.encrypt(PlainText(s))
encrypterDecrypter.encrypt(PlainText(s))

encrypterDecrypter.decrypt(Crypted(p))