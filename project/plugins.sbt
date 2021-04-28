resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(
  Resolver.ivyStylePatterns)

resolvers += "HMRC Releases" at "https://dl.bintray.com/hmrc/releases"
resolvers += "hmrc-releases" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/"
resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "2.15.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "2.2.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "1.14.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.1.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.7")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.13")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.24")
