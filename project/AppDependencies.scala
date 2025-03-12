import sbt.*

object AppDependencies {

  val bootstrapVersion: String = "9.11.0"
  val hmrcMongoVersion: String = "2.6.0"
  val playJsonDerivedCodesVersion: String = "11.0.0"
  val enumeratumPlayVersion: String = "1.8.2"

  lazy val microserviceDependencies: Seq[ModuleID] = {

    val compile: Seq[ModuleID] = Seq(
      "uk.gov.hmrc.mongo"   %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
      "uk.gov.hmrc"         %% "bootstrap-backend-play-30"  % bootstrapVersion,
      "com.beachape"        %% "enumeratum-play"            % enumeratumPlayVersion,
      "org.apache.pekko"    %% "pekko-connectors-csv"       % "1.0.2" //higher version not yet compatible with Play Framework 3.0.6
    )

    val test: Seq[ModuleID] = Seq(
      "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion
    ).map(_ % Test)

    compile ++ test
  }
}
