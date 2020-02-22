package org.scash.rpc.zrpc

import zio._

import org.scash.rpc.jsonmodels.{ GetBlockChainInfoResult }

trait ZBlockchainRpc {
  val rpc: ZBlockchainRpc.Service[Any]
}

object ZBlockchainRpc {
  trait Service[R] {
    def getBlockChainInfo: RIO[R, GetBlockChainInfoResult]
  }

}
