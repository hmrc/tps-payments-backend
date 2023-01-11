import sbt.Compile
import sbt.Keys.compile
import wartremover.WartRemover.autoImport.{wartremoverWarnings, wartremoverErrors}
import wartremover.Wart

object  WartRemoverSettings {

  lazy val wartRemoverWarning = {
    val warningWarts = Seq(
      Wart.JavaSerializable,
      Wart.StringPlusAny,
      Wart.AsInstanceOf,
      Wart.IsInstanceOf,
      Wart.Any
    )
    (Compile / compile / wartremoverWarnings) ++= warningWarts
  }
  lazy val wartRemoverError = {
    // Error
    val errorWarts = Seq(
      Wart.ArrayEquals,
      Wart.AnyVal,
      Wart.EitherProjectionPartial,
      Wart.Enumeration,
      Wart.ExplicitImplicitTypes,
      Wart.FinalVal,
      Wart.JavaConversions,
      Wart.JavaSerializable,
      Wart.LeakingSealed,
      Wart.MutableDataStructures,
      Wart.Null,
      Wart.OptionPartial,
      Wart.Recursion,
      Wart.Return,
//      Wart.TraversableOps,
      Wart.TryPartial,
      Wart.Var,
      Wart.While)

    (Compile / compile / wartremoverErrors) ++= errorWarts
  }
}
