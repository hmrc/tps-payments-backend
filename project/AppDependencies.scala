import sbt._


object AppDependencies {

  val bootstrapVersion: String = "7.15.0"
  val hmrcMongoVersion: String = "1.3.0"
  val playJsonDerivedCodesVersion: String = "7.0.0"
  val enumeratumVersion: String = "1.7.0"

  lazy val microserviceDependencies: Seq[ModuleID] = {

    val bootstrapVersion = "7.16.0"

    val compile: Seq[ModuleID] = Seq(
      "uk.gov.hmrc.mongo"   %% "hmrc-mongo-play-28"         % "1.3.0",
      "uk.gov.hmrc"         %% "bootstrap-backend-play-28"  % bootstrapVersion,
      "com.beachape"        %% "enumeratum"                 % "1.7.0",
      "com.lightbend.akka"  %% "akka-stream-alpakka-csv"    % "2.0.2"
    )

    val test: Seq[ModuleID] = Seq(
      "org.scalatest"          %% "scalatest"              % "3.2.16",
      "com.vladsch.flexmark"   %  "flexmark-all"           % "0.62.2",
      "org.scalatestplus.play" %% "scalatestplus-play"     % "5.1.0",
      "org.pegdown"            %  "pegdown"                % "1.6.0",
      "com.github.tomakehurst" %  "wiremock-standalone"    % "2.27.2",
      "uk.gov.hmrc"            %% "bootstrap-test-play-28" % bootstrapVersion
    ).map(_ % Test)

    compile ++ test
  }

}
