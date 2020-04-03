cancelable in Global := true

lazy val core = project in file("core")

lazy val scash = project
  .in(file("."))
  .aggregate(
    secp256k1jni,
    zcore,
    zcoretest,
    ztestkit,
    rpc,
    rpcTest
  )
  .settings(CommonSettings.settings: _*)
  .settings(crossScalaVersions := Nil)

lazy val zcore = project
  .in(file("zcore"))
  .settings(CommonSettings.prodSettings: _*)

lazy val zcoretest = project
  .in(file("zcoretest"))
  .enablePlugins()
  .settings(CommonSettings.testSettings: _*)
  .dependsOn(
    zcore,
    ztestkit % "test->test"
  )

lazy val ztestkit = project
  .in(file("ztestkit"))
  .enablePlugins()
  .settings(CommonSettings.prodSettings: _*)
  .dependsOn(zcore % "compile->compile;test->test")

lazy val rpc = project
  .in(file("rpc"))
  .settings(CommonSettings.prodSettings: _*)

lazy val secp256k1jni = project
  .in(file("secp256k1jni"))
  .settings(CommonSettings.prodSettings: _*)
  .settings(coverageEnabled := false)
  .enablePlugins()

lazy val testkit = project
  .in(file("testkit"))
  .enablePlugins()
  .settings(CommonSettings.prodSettings: _*)
  .dependsOn(
    core % "compile->compile;test->test",
    rpc
  )

lazy val coreTest = project
  .in(file("core-test"))
  .enablePlugins()
  .settings(CommonSettings.testSettings: _*)
  .settings(name := "scash-test")
  .dependsOn(
    core,
    testkit % "test->test"
  )

lazy val rpcTest = project
  .in(file("rpc-test"))
  .settings(CommonSettings.testSettings: _*)
  .dependsOn(
    core % "compile->compile",
    testkit
  )
