import sbt._

object Projects {
  val core = project in file("..") / "core"
  val rpc = project in file("..") / "rpc"
  val secp256k1jni = project in file("..") / "secp256k1jni "
}
