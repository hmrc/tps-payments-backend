import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}

val appName = "tps-payments-backend"

scalaVersion := "2.13.10"

val strictBuilding: SettingKey[Boolean] = StrictBuilding.strictBuilding //defining here so it can be set before running sbt like `sbt 'set Global / strictBuilding := true' ...`
StrictBuilding.strictBuildingSetting

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    resolvers                        ++= Seq(Resolver.jcenterRepo),
    libraryDependencies              ++= AppDependencies.microserviceDependencies,
    retrieveManaged                  :=  true,
    routesGenerator                  :=  InjectedRoutesGenerator,
    update / evictionWarningOptions  :=  EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(majorVersion := 1)
  .settings(ScalariformSettings())
  .settings(ScoverageSettings())
  .settings(WartRemoverSettings.wartRemoverSettings)
  .settings(wartremoverExcluded ++=
    (Compile / routes).value ++
      (baseDirectory.value / "test").get ++
      Seq(sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala"))
  .settings(SbtUpdatesSettings.sbtUpdatesSettings)
  .settings(PlayKeys.playDefaultPort := 9125)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(integrationTestSettings())
  .configs(IntegrationTest)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(Compile / scalacOptions -= "utf8")
  .settings(
    routesImport ++= Seq(
      "model._"
    ))
  .settings(
    scalacOptions ++= scalaCompilerOptions,
    scalacOptions ++= {
      if (StrictBuilding.strictBuilding.value) strictScalaCompilerOptions else Nil
    }
  )
  .settings(libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always) //otherwise scoverage pulls newer incompatible lib

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
