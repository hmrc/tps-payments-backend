resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += "hmrc-releases" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/"
resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")

addSbtPlugin("uk.gov.hmrc"       %  "sbt-auto-build"        % "2.15.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-git-versioning"    % "2.2.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-distributables"    % "2.1.0")
addSbtPlugin("com.typesafe.play" %  "sbt-plugin"            % "2.8.8")
addSbtPlugin("org.scoverage"     %  "sbt-scoverage"         % "1.6.1")
addSbtPlugin("org.wartremover"   %  "sbt-wartremover"       % "2.4.15")
addSbtPlugin("org.scalariform"   %  "sbt-scalariform"       % "1.8.3")
addSbtPlugin("ch.epfl.scala"     %  "sbt-scalafix"          % "0.9.24")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin" % "1.0.0")
