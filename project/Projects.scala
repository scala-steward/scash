import sbt._

object Projects {
  val core         = project in file("..") / "core"
  val secp256k1jni = project in file("..") / "secp256k1jni "
}
