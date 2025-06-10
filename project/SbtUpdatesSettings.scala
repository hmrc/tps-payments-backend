import com.timushev.sbt.updates.UpdatesKeys.dependencyUpdates
import com.timushev.sbt.updates.UpdatesPlugin.autoImport.{dependencyUpdatesFailBuild, dependencyUpdatesFilter, moduleFilterRemoveValue}
import sbt.Keys.*
import sbt.{Def, *}

object SbtUpdatesSettings {

  lazy val sbtUpdatesSettings: Seq[Def.Setting[?]] = Seq(
    dependencyUpdatesFailBuild := StrictBuilding.strictBuilding.value,
    (Compile / compile) := ((Compile / compile) dependsOn dependencyUpdates).value,
    dependencyUpdatesFilter -= moduleFilter("org.scala-lang"),
    dependencyUpdatesFilter -= moduleFilter("org.playframework"),
    //newest version of pekko-connectors-csv is not yet compatible with Play Framework 3.0.6
    dependencyUpdatesFilter -= moduleFilter("org.apache.pekko", "pekko-connectors-csv")
  )
}
