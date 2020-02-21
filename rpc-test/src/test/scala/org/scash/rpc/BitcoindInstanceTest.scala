package org.scash.rpc

import java.io.{File, PrintWriter}
import java.net.URI
import java.nio.file.{Files, Path}

import akka.stream.StreamTcpException
import org.scash.core.config.RegTest
import org.scash.core.currency.Bitcoins
import org.scash.rpc.client.common.BitcoindRpcClient
import org.scash.rpc.config.{
  BitcoindAuthCredentials,
  BitcoindConfig,
  BitcoindInstance
}
import org.scash.rpc.util.RpcUtil
import org.scash.testkit.rpc.BitcoindRpcTestUtil
import org.scash.testkit.rpc.BitcoindRpcTestUtil.newestBitcoindBinary
import org.scash.testkit.util.BitcoindRpcTest
import org.scalatest.compatible.Assertion

import scala.concurrent.Future
import scala.io.Source

class BitcoindInstanceTest extends BitcoindRpcTest {

  private val sampleConf: Seq[String] = {
    val source = Source.fromURL(getClass.getResource("/sample-bitcoin.conf"))
    source.getLines.toSeq
  }

  private val datadir: Path = Files.createTempDirectory(null)

  override protected def beforeAll(): Unit = {
    val confFile = new File(datadir.toString + "/bitcoin.conf")
    val pw = new PrintWriter(confFile)
    sampleConf.foreach(line => pw.write(line + "\n"))
    pw.close()
  }

  /**
    * Tests that the client can call the isStartedF method
    * without throwing and then start
    */
  private def testClientStart(client: BitcoindRpcClient): Future[Assertion] = {
    clientAccum += client
    for {
      firstStarted <- client.isStartedF
      _ <- client.start()
      secondStarted <- client.isStartedF

      _ <- client.getBalance
    } yield {
      assert(!firstStarted)
      assert(secondStarted)
    }
  }

  behavior of "BitcoindInstance"

  it should "start a bitcoind with cookie based authentication" in {
    val confStr = s"""
                     |regtest=1
                     |daemon=1
                     |port=${RpcUtil.randomPort}
                     |rpcport=${RpcUtil.randomPort}
    """.stripMargin

    val conf = BitcoindConfig(confStr, BitcoindRpcTestUtil.tmpDir())
    val instance = BitcoindInstance.fromConfig(conf, newestBitcoindBinary)
    assert(
      instance.authCredentials
        .isInstanceOf[BitcoindAuthCredentials.CookieBased])

    val cli = BitcoindRpcClient.withActorSystem(instance)
    testClientStart(cli)
  }

  it should "start a bitcoind with user and password based authentication" in {
    val confStr = s"""
                     |daemon=1
                     |regtest=1
                     |rpcuser=foobar
                     |rpcpassword=barfoo
                     |port=${RpcUtil.randomPort}
                     |rpcport=${RpcUtil.randomPort}
      """.stripMargin

    val conf = BitcoindConfig(confStr, BitcoindRpcTestUtil.tmpDir())
    val instance = BitcoindInstance.fromConfig(conf, newestBitcoindBinary)
    assert(
      instance.authCredentials
        .isInstanceOf[BitcoindAuthCredentials.PasswordBased])
    testClientStart(BitcoindRpcClient.withActorSystem(instance))
  }

  // the values in this conf was generated by executing
  // rpcauth.py from Bicoin Core like this:
  //
  // ❯ ./rpcauth.py bitcoin-s strong_password
  // String to be appended to bitcoin.conf:
  // rpcauth=bitcoin-s:6d7580be1deb4ae52bc4249871845b09$82b282e7c6493f6982a5a7af9fbb1b671bab702e2f31bbb1c016bb0ea1cc27ca
  // Your password:
  // strong_password

  it should "start a bitcoind with auth based authentication" in {
    val port = RpcUtil.randomPort
    val rpcPort = RpcUtil.randomPort
    val confStr = s"""
                     |daemon=1
                     |rpcauth=bitcoin-s:6d7580be1deb4ae52bc4249871845b09$$82b282e7c6493f6982a5a7af9fbb1b671bab702e2f31bbb1c016bb0ea1cc27ca
                     |regtest=1
                     |port=${RpcUtil.randomPort}
                     |rpcport=${RpcUtil.randomPort}
       """.stripMargin

    val conf = BitcoindConfig(confStr, BitcoindRpcTestUtil.tmpDir())
    val authCredentials =
      BitcoindAuthCredentials.PasswordBased(username = "bitcoin-s",
                                            password = "strong_password")
    val instance =
      BitcoindInstance(
        network = RegTest,
        uri = new URI(s"http://localhost:$port"),
        rpcUri = new URI(s"http://localhost:$rpcPort"),
        authCredentials = authCredentials,
        datadir = conf.datadir,
        binary = newestBitcoindBinary
      )

    testClientStart(BitcoindRpcClient.withActorSystem(instance))
  }
/*
  it should "parse a bitcoin.conf file, start bitcoind, mine some blocks and quit" in {
    val instance =
      BitcoindInstance.fromDatadir(datadir.toFile, newestBitcoindBinary)
    val client = BitcoindRpcClient.withActorSystem(instance)

    for {
      _ <- client.start()
      _ <- client.getNewAddress.flatMap(client.generateToAddress(101, _))
      balance <- client.getBalance
      _ <- BitcoindRpcTestUtil.stopServers(Vector(client))
      _ <- client.getBalance
        .map { balance =>
          logger.error(s"Got unexpected balance: $balance")
          fail("Was able to connect to bitcoind after shutting down")
        }
        .recover {
          case _: StreamTcpException =>
            ()
        }
    } yield assert(balance > Bitcoins(0))

  }
*/
}
