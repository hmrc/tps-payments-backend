import play.core.PlayVersion
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"   %% "simple-reactivemongo"      % "8.0.0-play-27",
    "uk.gov.hmrc"   %% "bootstrap-backend-play-27" % "4.1.0",
    "com.beachape" %% "enumeratum"                 % "1.5.15"
  )

  val test = Seq(
  "org.scalatest"           %% "scalatest"          % "3.2.1"             % Test,
  "com.vladsch.flexmark"    %  "flexmark-all"       % "0.35.10"           % Test, //required by scalatest
  "org.scalatestplus.play"  %% "scalatestplus-play" % "3.1.2"             % Test,
  "org.pegdown"             %  "pegdown"            % "1.6.0"             % Test,
  "com.github.tomakehurst"  %  "wiremock-jre8"      % "2.21.0"            % Test,
  "com.typesafe.play"       %% "play-test"          % PlayVersion.current % Test
  )

}
