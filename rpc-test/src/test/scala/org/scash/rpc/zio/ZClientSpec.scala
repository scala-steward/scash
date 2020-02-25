package org.scash.rpc.zio

import java.io.File
import java.net.URI
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{ Files, Path }

import org.scash.core.config.RegTest
import org.scash.rpc.config.{ BitcoindAuthCredentials, BitcoindInstance }
import zio._
import zio.console._
import zio.test._
import zio.test.Assertion._
import zio.test.environment._
import zio.{ Managed, RIO, Task, UIO }
import org.scash.rpc.zrpc._
import sttp.client._

object ZClientSpec
    extends DefaultRunnableSpec(
      suite("ZClient")(
        testM("ping") {
          val test = assertM(zrpc.ping, isUnit)
          test.provide(Utils.instance)
        },
        testM("getBlockCount") {
          val test = assertM(zrpc.getBlockCount, equalTo(2668))
          test.provide(Utils.instance)
        },
        testM("getBlockchainInfo") {
          val test = assertM(zrpc.getBlockChainInfo, equalTo(1))
          test.provide(Utils.instance)
        }
      )
    )

object Utils {
  val instance =
    ZConfig(
      userName = "user",
      passWord = "password",
      uri = uri"http://127.0.0.1:8332"
    )
}
