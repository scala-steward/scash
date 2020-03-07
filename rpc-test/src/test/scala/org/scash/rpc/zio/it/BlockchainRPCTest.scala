package org.scash.rpc.zio.it

import org.scash.core.crypto.DoubleSha256DigestBE
import org.scash.rpc.jsonmodels.{ GetBlockChainInfoResult, GetBlockResult }
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
        /*
        testM("getBlockWithTransactions") {
          val test = assertM(
            zrpc.getBlockWithTransactions(TestUtil.genesisBlockHash),
            isSubtype[GetBlockWithTransactionsResult](Assertion.anything)
          )
          test.provideM(TestUtil.instance)
        },
         */
        testM("getBlockchainInfo") {
          val test = assertM(zrpc.getBlockChainInfo, isSubtype[GetBlockChainInfoResult](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("getBlockCount") {
          val test = assertM(zrpc.getBlockCount, isSubtype[Int](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("getBlockHash") {
          val test = assertM(zrpc.getBlockHash(1), equalTo(TestUtil.genesisBlockHash))
          test.provideManaged(TestUtil.instance)
        }
      ) @@ TestAspect.sequential
    )
