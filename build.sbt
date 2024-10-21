val appName = "tps-payments-backend"

val scalaV = "2.13.12"
scalaVersion := scalaV
val majorVer = 2
majorVersion := majorVer

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(commonSettings: _*)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion                     := majorVer,
    scalaVersion                     := scalaV,
    libraryDependencies              ++= AppDependencies.microserviceDependencies,
    routesGenerator                  :=  InjectedRoutesGenerator,
    update / evictionWarningOptions  :=  EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(WartRemoverSettings.wartRemoverSettingsPlay)
  .dependsOn(corJourney, corJourneyTestData)
  .aggregate(corJourney, corJourneyTestData)
  .settings(PlayKeys.playDefaultPort := 9125)
  .settings(
    routesImport ++= Seq(
      "tps.model._",
      "tps.journey.model._",
      "tps.utils._"
    ))
  .settings(
    commands += Command.command("runTestOnly") { state =>
      state.globalLogging.full.info("running play using 'testOnlyDoNotUseInAppConf' routes...")
      s"""set javaOptions += "-Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"""" ::
        "run" ::
        s"""set javaOptions -= "-Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"""" ::
        state
    }
  )


lazy val corJourney = Project(appName + "-cor-journey", file("cor-journey"))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(commonSettings: _*)
  .settings(
    scalaVersion := scalaV,
    majorVersion := majorVer,
    libraryDependencies ++= List(
      "uk.gov.hmrc"       %% "auth-client-play-30"      % "8.2.0",
      "uk.gov.hmrc"       %% "bootstrap-common-play-30" % AppDependencies.bootstrapVersion % Provided,
      "org.julienrf"      %% "play-json-derived-codecs" % AppDependencies.playJsonDerivedCodesVersion, //choose carefully
      "com.beachape"      %% "enumeratum-play"          % "1.8.1",
      "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"       % AppDependencies.hmrcMongoVersion //for java Instant Json Formats
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
      "org.playframework" %% "play"      % play.core.PlayVersion.current % Provided,
      "org.playframework" %% "play-test" % play.core.PlayVersion.current % Provided
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
  libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
  scalacOptions ++= scalaCompilerOptions,
  scalacOptions ++= {
    if (StrictBuilding.strictBuilding.value) strictScalaCompilerOptions else Nil
  }
)
  .++(ScalariformSettings())
  .++(ScoverageSettings())
  .++(WartRemoverSettings.wartRemoverSettings)
  .++(uk.gov.hmrc.DefaultBuildSettings.scalaSettings)
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

