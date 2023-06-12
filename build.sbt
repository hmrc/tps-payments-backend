import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}

val appName = "tps-payments-backend"

val scalaV = "2.13.10"
scalaVersion := scalaV
val majorVer = 2
majorVersion := majorVer

lazy val microservice2 = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(commonSettings *)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion                     := majorVer,
    scalaVersion                     := scalaV,
    libraryDependencies              ++= AppDependencies.microserviceDependencies,
    //otherwise scoverage pulls newer incompatible lib:
    libraryDependencySchemes         += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    routesGenerator                  :=  InjectedRoutesGenerator,
    update / evictionWarningOptions  :=  EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(WartRemoverSettings.wartRemoverSettingsPlay)
  .dependsOn(corJourney, corJourneyTestData)
  .aggregate(corJourney, corJourneyTestData)
  .settings(PlayKeys.playDefaultPort := 9125)
  .settings(
    routesImport ++= Seq(
      "model._"
    ))


lazy val corJourney = Project(appName + "-cor-journey", file("cor-journey"))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(commonSettings: _*)
  .settings(
    scalaVersion := scalaV,
    majorVersion := majorVer,
    libraryDependencies ++= List(
      "uk.gov.hmrc"       %% "auth-client"              % "6.1.0-play-28",
      "uk.gov.hmrc"       %% "bootstrap-common-play-28" % AppDependencies.bootstrapVersion % Provided,
      "org.julienrf"      %% "play-json-derived-codecs" % AppDependencies.playJsonDerivedCodesVersion, //choose carefully
      "com.beachape"      %% "enumeratum-play"          % AppDependencies.enumeratumVersion,
      "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"       % AppDependencies.hmrcMongoVersion //for java Instant Json Formats
    )
  )

/**
 * Collection Of Routines - test data
 */
lazy val corJourneyTestData = Project(appName + "-cor-journey-test-data", file("cor-journey-test-data"))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(commonSettings: _*)
  .settings(
    scalaVersion := scalaV,
    majorVersion := majorVer,
    libraryDependencies ++= List(
      "com.typesafe.play" %% "play"      % play.core.PlayVersion.current % Provided,
      "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % Provided
    )
  )
  .dependsOn(corJourney)
  .aggregate(corJourney)

lazy val commonSettings: Seq[Def.SettingsDefinition] = Seq(
  majorVersion := majorVer,
  resolvers += Resolver.jcenterRepo,
  Compile / doc / scalacOptions := Seq(), //this will allow to have warnings in `doc` task
  Test / doc / scalacOptions := Seq(), //this will allow to have warnings in `doc` task
  Compile / scalacOptions -= "utf8",
  scalacOptions ++= scalaCompilerOptions,
  scalacOptions ++= {
    if (StrictBuilding.strictBuilding.value) strictScalaCompilerOptions else Nil
  }
)
  .++(ScalariformSettings())
  .++(ScoverageSettings())
  .++(WartRemoverSettings.wartRemoverSettings)
  .++(scalaSettings)
  .++(uk.gov.hmrc.DefaultBuildSettings.defaultSettings())
  .++(SbtUpdatesSettings.sbtUpdatesSettings)

lazy val scalaCompilerOptions: Seq[String] = Seq(
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-Wconf:cat=unused-imports&src=html/.*:s",
  "-Wconf:src=routes/.*:s"
)

lazy val strictScalaCompilerOptions: Seq[String] = Seq(
  "-Xfatal-warnings",
  "-Xlint:-missing-interpolator,_",
  "-Xlint:adapted-args",
  "-Xlint:constant",
  "-Xlint:-byname-implicit",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:explicits",
  "-Ywarn-unused:params",
  "-Ywarn-unused:implicits",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Wconf:cat=unused-imports&src=html/.*:s",
  "-Wconf:src=routes/.*:s"
)

lazy val strictBuilding: SettingKey[Boolean] = StrictBuilding.strictBuilding //defining here so it can be set before running sbt like `sbt 'set Global / strictBuilding := true' ...`
StrictBuilding.strictBuildingSetting

