package org.scash.rpc.zio.it

import org.scash.core.crypto.DoubleSha256DigestBE
import org.scash.core.protocol.blockchain.{ Block, BlockHeader }
import org.scash.rpc.jsonmodels.{
  ChainTip,
  GetBlockChainInfoResult,
  GetBlockHeaderResult,
  GetBlockResult,
  GetBlockWithTransactionsResult
}
import org.scash.rpc.zio.TestUtil
import org.scash.rpc.zrpc.zrpc
import zio.test.Assertion.{ equalTo, isSubtype }
import zio.test._

object BlockchainRPCTest
    extends DefaultRunnableSpec(
      suite("BlockchainRPC")(
        testM("getBestBlockHash") {
          val test = assertM(zrpc.getBestBlockHash, isSubtype[DoubleSha256DigestBE](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("getBlock") {
          val test = assertM(zrpc.getBlock(TestUtil.genesisBlockHash), isSubtype[GetBlockResult](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("getBlockWithTransactions") {
          val test = assertM(
            zrpc.getBlockWithTransactions(TestUtil.test1Hash),
            isSubtype[GetBlockWithTransactionsResult](Assertion.anything)
          )
          test.provideManaged(TestUtil.instance)
        },
        testM("getBlockchainInfo") {
          val test = assertM(zrpc.getBlockChainInfo, isSubtype[GetBlockChainInfoResult](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        }, /*
        testM("getBlockCount") {
          val test = assertM(zrpc.getBlockCount, isSubtype[Int](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },*/
        testM("getBlockHash") {
          val test = assertM(zrpc.getBlockHash(1), equalTo(TestUtil.genesisBlockHash))
          test.provideManaged(TestUtil.instance)
        },
        testM("getBlockHeader") {
          val test =
            assertM(zrpc.getBlockHeader(TestUtil.genesisBlockHash), isSubtype[GetBlockHeaderResult](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("getBlockHeaderRaw") {
          val test =
            assertM(zrpc.getBlockHeaderRaw(TestUtil.genesisBlockHash), isSubtype[BlockHeader](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("getBlock") {
          val test = assertM(zrpc.getBlockRaw(TestUtil.genesisBlockHash), isSubtype[Block](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("getChainTips") {
          val test = assertM(zrpc.getChainTips, isSubtype[Vector[ChainTip]](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        }
      ) @@ TestAspect.sequential
    )
