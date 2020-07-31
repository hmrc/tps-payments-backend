import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import wartremover.{Wart, wartremoverErrors, wartremoverExcluded}

val appName = "tps-payments-backend"

val akkaVersion     = "2.5.23"
val akkaHttpVersion = "10.0.15"

dependencyOverrides += "com.typesafe.akka" %% "akka-stream"    % akkaVersion
dependencyOverrides += "com.typesafe.akka" %% "akka-protobuf"  % akkaVersion
dependencyOverrides += "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion
dependencyOverrides += "com.typesafe.akka" %% "akka-actor"     % akkaVersion
dependencyOverrides += "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion


lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    resolvers                        ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo),
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    retrieveManaged                  :=  true,
    routesGenerator                  :=  InjectedRoutesGenerator,
    evictionWarningOptions in update :=  EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(scalaVersion := "2.12.11")
  .settings(majorVersion := 1)
  .settings(ScalariformSettings())
  .settings(ScoverageSettings())
  .settings(WartRemoverSettings.wartRemoverError)
  .settings(WartRemoverSettings.wartRemoverWarning)
  .settings(wartremoverErrors in(Test, compile) --= Seq(Wart.Any, Wart.Equals, Wart.Null, Wart.NonUnitStatements, Wart.PublicInference))
  .settings(wartremoverExcluded ++=
    routes.in(Compile).value ++
      (baseDirectory.value / "test").get ++
      Seq(sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala"))
  .settings(publishingSettings: _*)
  .settings(PlayKeys.playDefaultPort := 9125)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(integrationTestSettings())
  .configs(IntegrationTest)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(
    routesImport ++= Seq(
      "model._"
    ))
  .settings(
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-Xlint:-missing-interpolator,_",
      "-Yno-adapted-args",
      "-Ywarn-value-discard",
      "-Ywarn-dead-code",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:implicitConversions",
      "-language:reflectiveCalls",
      "-Ypartial-unification" //required by cats
    )
  )
