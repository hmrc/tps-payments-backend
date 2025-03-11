import play.sbt.routes.RoutesKeys.routes
import sbt.Keys.*
import sbt.{Compile, Test, *}
import wartremover.Wart
import wartremover.WartRemover.autoImport.*

object  WartRemoverSettings {

  val wartRemoverSettings =
    Seq(
      (Compile / compile / wartremoverErrors) ++= {
        if (StrictBuilding.strictBuilding.value) Warts.allBut(
          Wart.DefaultArguments,
          Wart.ImplicitConversion,
          Wart.ImplicitParameter,
          Wart.Nothing,
          Wart.Overloading,
          Wart.SizeIs,
          Wart.SortedMaxMinOption,
          Wart.Throw,
          Wart.ToString,
          Wart.PlatformDefault,
          Wart.Product,
          Wart.JavaSerializable,
          Wart.Serializable
        )
        else Nil
      },
      Test / compile / wartremoverErrors --= Seq(
        Wart.Any,
        Wart.Equals,
        Wart.GlobalExecutionContext,
        Wart.Null,
        Wart.NonUnitStatements,
        Wart.PublicInference
      )
    )

  lazy val wartRemoverSettingsPlay = Seq(
    wartremoverExcluded ++= (Compile / routes).value ++ Seq(
      sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala"
    )
  )
}
