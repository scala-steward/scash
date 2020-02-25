package org.scash.rpc.zrpc

import org.scash.rpc.jsonmodels.GetBlockChainInfoResult
import org.scash.rpc.serializers.JsonSerializers._
import zio.{ RIO, ZIO }

package object zrpc {

  def ping: RIO[ZClient, Unit] =
    ZIO.accessM[ZClient](_.rpc.bitcoindCall[Unit]("ping"))

  def getBlockCount: RIO[ZClient, Int] =
    ZIO.accessM[ZClient](_.rpc.bitcoindCall[Int]("getblockcount"))

  def getBlockChainInfo: RIO[ZClient, GetBlockChainInfoResult] =
    ZIO.accessM[ZClient](_.rpc.bitcoindCall[GetBlockChainInfoResult]("getblockchaininfo"))

}
