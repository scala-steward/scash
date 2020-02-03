name := "rpc-test"

libraryDependencies ++= Deps.rpcTest(scalaVersion.value)

Test / test := (Test / test dependsOn {
  Projects.rpc / TaskKeys.downloadBitcoind
}).value
