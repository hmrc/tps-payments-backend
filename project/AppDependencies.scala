import play.core.PlayVersion
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "simple-reactivemongo" % "7.31.0-play-27",
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "3.3.0",
    "com.beachape" %% "enumeratum" % "1.6.1"
  )


  val test = Seq(
  "org.scalatest" %% "scalatest" % "3.2.1" % Test,
  "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % Test, //required by scalatest
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test, //watch out version, 3.xx introduces play 2.6
  "org.pegdown" % "pegdown" % "1.6.0" % Test,
  "com.github.tomakehurst" % "wiremock-jre8" % "2.21.0" % Test,
  "com.typesafe.play" %% "play-test" % PlayVersion.current % Test
  )

}
