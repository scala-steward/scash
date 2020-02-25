package org.scash.rpc.zrpc

import org.scash.rpc.jsonmodels.GetBlockChainInfoResult

import org.scash.rpc.serializers.JsonSerializers._
import zio.RIO

package object zrpc {

  def ping: RIO[ZConfig, Unit] = ClientService.bitcoindCall[Unit]("ping")

  def getBlockChainInfo: RIO[ZConfig, GetBlockChainInfoResult] =
    ClientService.bitcoindCall[GetBlockChainInfoResult]("getblockchaininfo")

  def getBlockCount: RIO[ZConfig, Int] =
    ClientService.bitcoindCall[Int]("getblockcount")
}
