package org.scash.rpc.zrpc

import org.scash.core.crypto.{ DoubleSha256Digest, DoubleSha256DigestBE }
import org.scash.core.protocol.blockchain.Block
import org.scash.rpc.jsonmodels.{ GetBlockChainInfoResult, GetBlockResult, GetBlockWithTransactionsResult }
import org.scash.rpc.serializers.JsonSerializers._
import play.api.libs.json.{ JsNumber, JsString }
import zio.{ RIO, ZIO }

trait BlockchainRPC {
  def ping: RIO[ZClient, Unit] =
    ZIO.accessM[ZClient](_.rpc.bitcoindCall[Unit]("ping"))

  def getBestBlockHash: RIO[ZClient, DoubleSha256DigestBE] =
    ZIO.accessM[ZClient](_.rpc.bitcoindCall[DoubleSha256DigestBE]("getbestblockhash"))

  /**
   * Returns a `GetBlockResult` from the block <hash>, with a Vector of tx hashes inside the block
   * Equivalent to bitcoin-cli getblock <hash> 1`
   */
  def getBlock(headerHash: DoubleSha256DigestBE): RIO[ZClient, GetBlockResult] =
    ZIO.accessM[ZClient](_.rpc.bitcoindCall[GetBlockResult]("getblock", List(JsString(headerHash.hex), JsNumber(1))))

  /**
   * Same as [[getBlock]] but takes a Little Endian DoubleSha256 Hex instead of Big endian (BE)
   */
  def getBlock(headerHash: DoubleSha256Digest): RIO[ZClient, GetBlockResult] = getBlock(headerHash.flip)

  /**
   * Returns a `GetBlockWithTransactionsResult` from the block <hash>, with a Vector of tx `RpcTransaction`
   * Equivalent to bitcoin-cli getblock <hash> 2
   */
  def getBlockWithTransactions(headerHash: DoubleSha256DigestBE): RIO[ZClient, GetBlockWithTransactionsResult] =
    ZIO.accessM[ZClient](
      _.rpc.bitcoindCall[GetBlockWithTransactionsResult]("getblock", List(JsString(headerHash.hex), JsNumber(2)))
    )

  def getBlockChainInfo: RIO[ZClient, GetBlockChainInfoResult] =
    ZIO.accessM[ZClient](_.rpc.bitcoindCall[GetBlockChainInfoResult]("getblockchaininfo"))

  def getBlockCount: RIO[ZClient, Int] =
    ZIO.accessM[ZClient](_.rpc.bitcoindCall[Int]("getblockcount"))

  def getBlockHash(height: Int): RIO[ZClient, DoubleSha256DigestBE] =
    ZIO.accessM[ZClient](_.rpc.bitcoindCall[DoubleSha256DigestBE]("getblockhash", List(JsNumber(height))))

}
