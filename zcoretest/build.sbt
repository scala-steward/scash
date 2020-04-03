name := "zcoretest"

libraryDependencies ++= Deps.zcoretest

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
