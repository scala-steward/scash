package org.scash.rpc

import java.net.URI

import akka.stream.StreamTcpException
import org.scash.core.config.RegTest
import org.scash.core.currency.Bitcoins
import org.scash.rpc.client.common.BitcoindRpcClient
import org.scash.rpc.config.{ BitcoindAuthCredentials, BitcoindInstance }
import org.scash.rpc.jsonmodels.GetBlockChainInfoResult
import org.scash.rpc.zrpc.ZClient
import org.scash.testkit.rpc.BitcoindRpcTestUtil
import org.scash.testkit.rpc.BitcoindRpcTestUtil.newestBitcoindBinary
import org.scash.testkit.util.BitcoinSAsyncTest
import org.scash.rpc.jsonmodels._
import org.scash.rpc.serializers.JsonSerializers._
import scala.io.Source

class ZClientTest extends BitcoinSAsyncTest {
  val instance =
    BitcoindInstance(
      network = RegTest,
      uri = new URI(s"http://localhost:9000"),
      rpcUri = new URI(s"http://localhost:18500"),
      authCredentials = BitcoindAuthCredentials.PasswordBased(username = "user", password = "password")
    )

  it should "parse a bitcoin.conf file, start bitcoind, mine some blocks and quit" in {
    val client = ZClient(instance)

    val e = client.bitcoindCall[GetBlockChainInfoResult]("getblockchaininfo")
    e.provide(client).run
  }
}
