package org.scash.rpc.zrpc

import org.scash.core.crypto.{ DoubleSha256Digest, DoubleSha256DigestBE }
import org.scash.core.protocol.blockchain.{ Block, BlockHeader }
import org.scash.rpc.jsonmodels.{
  ChainTip,
  GetBlockChainInfoResult,
  GetBlockHeaderResult,
  GetBlockResult,
  GetBlockWithTransactionsResult
}
import org.scash.rpc.serializers.JsonSerializers._
import org.scash.rpc.zrpc.zrpc.ZClient
import play.api.libs.json.{ JsBoolean, JsNumber, JsString }
import zio.ZIO

trait BlockchainRPC {
  def getBestBlockHash: ZIO[ZClient, BitcoindError, DoubleSha256DigestBE] =
    ZIO.accessM[ZClient](_.get.bitcoindCall[DoubleSha256DigestBE]("getbestblockhash"))

  /**
   * Returns a `GetBlockResult` from the block <hash>, with a Vector of tx hashes inside the block
   * Equivalent to bitcoin-cli getblock <hash> 1`
   */
  def getBlock(headerHash: DoubleSha256DigestBE): ZIO[ZClient, BitcoindError, GetBlockResult] =
    ZIO.accessM[ZClient](
      _.get.bitcoindCall[GetBlockResult]("getblock", List(JsString(headerHash.hex), JsNumber(1)))
    )

  /**
   * Same as [[getBlock]] but takes a Little Endian DoubleSha256 Hex instead of Big endian (BE)
   */
  def getBlock(headerHash: DoubleSha256Digest): ZIO[ZClient, BitcoindError, GetBlockResult] = getBlock(headerHash.flip)

  /**
   * Returns a `GetBlockWithTransactionsResult` from the block <hash>, with a Vector of tx `RpcTransaction`
   * Equivalent to bitcoin-cli getblock <hash> 2
   */
  def getBlockWithTransactions(
    headerHash: DoubleSha256DigestBE
  ): ZIO[ZClient, BitcoindError, GetBlockWithTransactionsResult] =
    ZIO.accessM[ZClient](
      _.get.bitcoindCall[GetBlockWithTransactionsResult]("getblock", List(JsString(headerHash.hex), JsNumber(2)))
    )

  def getBlockWithTransactions(
    headerHash: DoubleSha256Digest
  ): ZIO[ZClient, BitcoindError, GetBlockWithTransactionsResult] =
    getBlockWithTransactions(headerHash.flip)

  def getBlockChainInfo: ZIO[ZClient, BitcoindError, GetBlockChainInfoResult] =
    ZIO.accessM[ZClient](_.get.bitcoindCall[GetBlockChainInfoResult]("getblockchaininfo"))

  def getBlockCount: ZIO[ZClient, BitcoindError, Int] =
    ZIO.accessM[ZClient](_.get.bitcoindCall[Int]("getblockcount"))

  def getBlockHash(height: Int): ZIO[ZClient, BitcoindError, DoubleSha256DigestBE] =
    ZIO.accessM[ZClient](_.get.bitcoindCall[DoubleSha256DigestBE]("getblockhash", List(JsNumber(height))))

  def getBlockHeader(headerHash: DoubleSha256DigestBE): ZIO[ZClient, BitcoindError, GetBlockHeaderResult] =
    ZIO.accessM[ZClient](
      _.get.bitcoindCall[GetBlockHeaderResult]("getblockheader", List(JsString(headerHash.hex), JsBoolean(true)))
    )

  def getBlockHeader(headerHash: DoubleSha256Digest): ZIO[ZClient, BitcoindError, GetBlockHeaderResult] =
    getBlockHeader(headerHash.flip)

  def getBlockHeaderRaw(headerHash: DoubleSha256DigestBE): ZIO[ZClient, BitcoindError, BlockHeader] =
    ZIO.accessM[ZClient](
      _.get.bitcoindCall[BlockHeader]("getblockheader", List(JsString(headerHash.hex), JsBoolean(false)))
    )

  def getBlockHeaderRaw(headerHash: DoubleSha256Digest): ZIO[ZClient, BitcoindError, BlockHeader] =
    getBlockHeaderRaw(headerHash.flip)

  def getBlockRaw(headerHash: DoubleSha256DigestBE): ZIO[ZClient, BitcoindError, Block] =
    ZIO.accessM[ZClient](_.get.bitcoindCall[Block]("getblock", List(JsString(headerHash.hex), JsNumber(0))))

  def getBlockRaw(headerHash: DoubleSha256Digest): ZIO[ZClient, BitcoindError, Block] =
    getBlockRaw(headerHash.flip)

  def getChainTips: ZIO[ZClient, BitcoindError, Vector[ChainTip]] =
    ZIO.accessM[ZClient](_.get.bitcoindCall[Vector[ChainTip]]("getchaintips"))
}
