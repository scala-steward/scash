package org.scash.rpc.zio

import org.scash.core.crypto
import org.scash.core.crypto.DoubleSha256DigestBE
import org.scash.rpc.jsonmodels.{ GetBlockChainInfoResult, GetBlockResult, GetBlockWithTransactionsResult }
import org.scash.rpc.zrpc._
import zio.test._
import zio.test.Assertion._
import sttp.client._

object BlockchainRPCTest
    extends DefaultRunnableSpec(
      suite("BlockchainRPC")(
        testM("ping") {
          val test = assertM(zrpc.ping, isUnit)
          test.provideM(Utils.instance)
        },
        testM("getBestBlockHash") {
          val test = assertM(zrpc.getBestBlockHash, isSubtype[DoubleSha256DigestBE](Assertion.anything))
          test.provideM(Utils.instance)
        },
        testM("getBlock") {
          val test = assertM(zrpc.getBlock(Utils.genesisBlockHash), isSubtype[GetBlockResult](Assertion.anything))
          test.provideM(Utils.instance)
        },
        /*
        testM("getBlockWithTransactions") {
          val test = assertM(
            zrpc.getBlockWithTransactions(Utils.genesisBlockHash),
            isSubtype[GetBlockWithTransactionsResult](Assertion.anything)
          )
          test.provideM(Utils.instance)
        },
         */
        testM("getBlockchainInfo") {
          val test = assertM(zrpc.getBlockChainInfo, isSubtype[GetBlockChainInfoResult](Assertion.anything))
          test.provideM(Utils.instance)
        },
        testM("getBlockCount") {
          val test = assertM(zrpc.getBlockCount, isSubtype[Int](Assertion.anything))
          test.provideM(Utils.instance)
        },
        testM("getBlockHash") {
          val test = assertM(zrpc.getBlockHash(1), equalTo(Utils.genesisBlockHash))
          test.provideM(Utils.instance)
        }
      )
    )

object Utils {

  val test1Hash        = DoubleSha256DigestBE("000000000000000002010fbeac4ccbb5ad3abafe684228219134bb5354978644")
  val genesisBlockHash = DoubleSha256DigestBE("00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048")

  val instance = ZClient.Live.make(
    uri"http://127.0.0.1:8332",
    "user",
    "password"
  )
}
