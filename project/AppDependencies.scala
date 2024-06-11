import sbt._

object AppDependencies {

  val bootstrapVersion: String = "9.5.0"
  val hmrcMongoVersion: String = "2.2.0"
  val playJsonDerivedCodesVersion: String = "11.0.0"

  lazy val microserviceDependencies: Seq[ModuleID] = {

    val compile: Seq[ModuleID] = Seq(
      "uk.gov.hmrc.mongo"   %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
      "uk.gov.hmrc"         %% "bootstrap-backend-play-30"  % bootstrapVersion,
      "com.beachape"        %% "enumeratum"                 % "1.7.4",
      "org.apache.pekko"    %% "pekko-connectors-csv"       % "1.0.2"
    )

    val test: Seq[ModuleID] = Seq(
      "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion,
      "org.pegdown" %  "pegdown"                % "1.6.0"
    ).map(_ % Test)

    compile ++ test
  }

}
