import play.core.PlayVersion
import play.sbt.PlayImport.caffeine
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-play-28"         % "0.73.0",
    "uk.gov.hmrc"         %% "bootstrap-backend-play-28"  % "5.4.0",
    "com.beachape"        %% "enumeratum"                 % "1.7.0",
    "com.lightbend.akka"  %% "akka-stream-alpakka-csv"    % "2.0.1"
  )


  val test = Seq(
  "org.scalatest"          %% "scalatest"          % "3.2.9"             % Test,
  "com.vladsch.flexmark"   %  "flexmark-all"       % "0.36.8"           % Test, //required by scalatest
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"             % Test,
  "org.pegdown"            %  "pegdown"            % "1.6.0"             % Test,
  "com.github.tomakehurst" %  "wiremock-jre8"      % "2.21.0"            % Test,
  "com.typesafe.play"      %% "play-test"          % PlayVersion.current % Test
  )

}
