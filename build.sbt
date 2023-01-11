import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import wartremover.Wart
import wartremover.WartRemover.autoImport.wartremoverErrors
import wartremover.WartRemover.autoImport.wartremoverExcluded

val appName = "tps-payments-backend"

scalaVersion := "2.13.8"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
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
  .settings(WartRemoverSettings.wartRemoverError)
  .settings(WartRemoverSettings.wartRemoverWarning)
  .settings((Test / compile / wartremoverErrors) --= Seq(Wart.Any, Wart.Equals, Wart.Null, Wart.NonUnitStatements, Wart.PublicInference))
  .settings(wartremoverExcluded ++=
    (Compile / routes).value ++
      (baseDirectory.value / "test").get ++
      Seq(sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala"))
  .settings(SbtUpdatesSettings.sbtUpdatesSettings)
  .settings(publishingSettings: _*)
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
    scalacOptions ++= Seq(
      "-Ywarn-unused:-imports,-patvars,-privates,-locals,-explicits,-implicits,_",
      "-Xfatal-warnings",
      "-Xlint:-missing-interpolator,_",
      "-Xlint:adapted-args",
      "-Xlint:-byname-implicit",
      "-Ywarn-value-discard",
      "-Ywarn-dead-code",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:implicitConversions",
      "-language:reflectiveCalls"
    )
  )
  .settings(dependencyOverrides += "org.scala-lang.modules" % "scala-xml_2.13" % "2.1.0")
