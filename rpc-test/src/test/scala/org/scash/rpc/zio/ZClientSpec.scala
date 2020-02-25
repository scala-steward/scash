package org.scash.rpc.zio

import org.scash.rpc.jsonmodels.GetBlockChainInfoResult
import org.scash.rpc.zrpc._

import zio.test._
import zio.test.Assertion._

import sttp.client._

object ZClientSpec
    extends DefaultRunnableSpec(
      suite("ZClient")(
        testM("ping") {
          val test = assertM(zrpc.ping, isUnit)
          test.provideM(Utils.instance)
        },
        testM("getBlockCount") {
          val test = assertM(zrpc.getBlockCount, isSubtype[Int](Assertion.anything))
          test.provideM(Utils.instance)
        },
        testM("getBlockchainInfo") {
          val test = assertM(zrpc.getBlockChainInfo, isSubtype[GetBlockChainInfoResult](Assertion.anything))
          test.provideM(Utils.instance)
        }
      )
    )

object Utils {
  val instance = ZClient.Live.make(
    uri"http://127.0.0.1:8332",
    "user",
    "password"
  )
}
