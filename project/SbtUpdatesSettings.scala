import com.timushev.sbt.updates.Compat.ModuleFilter
import com.timushev.sbt.updates.UpdatesKeys.dependencyUpdates
import com.timushev.sbt.updates.UpdatesPlugin.autoImport.{dependencyUpdatesFailBuild, dependencyUpdatesFilter, moduleFilterRemoveValue}
import sbt.Keys._
import sbt.{Def, _}
import xsbti.compile.CompileAnalysis

object SbtUpdatesSettings {

  lazy val sbtUpdatesSettings: Seq[Def.Setting[_ >: Boolean with Task[CompileAnalysis] with ModuleFilter]] = Seq(
    dependencyUpdatesFailBuild := StrictBuilding.strictBuilding.value,
    (Compile / compile) := ((Compile / compile) dependsOn dependencyUpdates).value,
    dependencyUpdatesFilter -= moduleFilter("org.scala-lang"),
    dependencyUpdatesFilter -= moduleFilter("org.playframework"),
    // I have had to add enumeratum to the ignore list, due to:
    // java.lang.NoSuchMethodError: 'scala.Option play.api.libs.json.JsBoolean$.unapply(play.api.libs.json.JsBoolean)'
    // error on 1.7.2
    dependencyUpdatesFilter -= moduleFilter("com.beachape", "enumeratum"),
    dependencyUpdatesFilter -= moduleFilter("com.beachape", "enumeratum-play")
  )

}
