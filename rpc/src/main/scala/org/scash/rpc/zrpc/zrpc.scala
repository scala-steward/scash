package org.scash.rpc.zrpc

import org.scash.rpc.jsonmodels.{ GetBlockChainInfoResult }
import zio.RIO

package object zrpc extends ZBlockchainRpc.Service[ZBlockchainRpc] {
  def getBlockChainInfo: RIO[ZBlockchainRpc, GetBlockChainInfoResult] =
    RIO.accessM(_.rpc.getBlockChainInfo)

}
