package org.scash.rpc.zrpc

import java.net.URI

import zio.{ RIO, ZIO }

import org.scash.rpc.client.common.RpcOpts.SetBanCommand
import org.scash.rpc.jsonmodels._
import org.scash.rpc.serializers.JsonSerializers._

import play.api.libs.json.{ JsBoolean, JsNumber, JsString }
import sttp.model.Uri

trait NetworkRPC {

  def addNode(address: URI): ZIO[ZClient, Throwable, Unit] = addNode(address, "add")

  def tryAddNodeOnce(address: URI): ZIO[ZClient, Throwable, Unit] = addNode(address, "onetry")

  def removeNode(address: URI): ZIO[ZClient, Throwable, Unit] = addNode(address, "remove")

  def clearBanned: ZIO[ZClient, Throwable, Unit] =
    ZIO.accessM[ZClient](_.zclient.bitcoindCall[Unit]("clearbanned"))

  def disconnectNode(address: URI): ZIO[ZClient, Throwable, Unit] =
    ZIO.accessM[ZClient](_.zclient.bitcoindCall[Unit]("disconnectnode", List(JsString(address.getAuthority))))

  def disconnectNode(nodeId: Int): ZIO[ZClient, Throwable, Unit] =
    ZIO.accessM[ZClient](_.zclient.bitcoindCall[Unit]("disconnectnode", List(JsString(""), JsNumber(nodeId))))

  def getAllAddedNodes: ZIO[ZClient, Throwable, Vector[Node]] = getAddedNodeInfo(None)

  def getAddedNode(node: URI): ZIO[ZClient, Throwable, Vector[Node]] =
    getAddedNodeInfo(Some(node))

  def getConnectionCount: ZIO[ZClient, Throwable, Int] =
    ZIO.accessM[ZClient](_.zclient.bitcoindCall[Int]("getconnectioncount"))

  def getExcessiveBlock: ZIO[ZClient, Throwable, GetExcessiveBlockSize] =
    ZIO.accessM[ZClient](_.zclient.bitcoindCall[GetExcessiveBlockSize]("getexcessiveblock"))

  def getNetTotals: ZIO[ZClient, Throwable, GetNetTotalsResult] =
    ZIO.accessM[ZClient](_.zclient.bitcoindCall[GetNetTotalsResult]("getnettotals"))

  def getNetworkInfo: ZIO[ZClient, Throwable, GetNetworkInfoResult] =
    ZIO.accessM[ZClient](_.zclient.bitcoindCall[GetNetworkInfoResult]("getnetworkinfo"))

  def getNodeAddresses(count: Int = 1): ZIO[ZClient, Throwable, Vector[GetNodeAddressesResult]] =
    ZIO.accessM[ZClient](
      _.zclient.bitcoindCall[Vector[GetNodeAddressesResult]]("getnodeaddresses", List(JsNumber(count)))
    )

  def getPeerInfo: ZIO[ZClient, Throwable, Vector[Peer]] =
    ZIO.accessM[ZClient](_.zclient.bitcoindCall[Vector[Peer]]("getpeerinfo"))

  def listBanned: ZIO[ZClient, Throwable, Vector[NodeBan]] =
    ZIO.accessM[ZClient](_.zclient.bitcoindCall[Vector[NodeBan]]("listbanned"))

  def ping: RIO[ZClient, Unit] =
    ZIO.accessM[ZClient](_.zclient.bitcoindCall[Unit]("ping"))

  def setBan(
    address: Uri,
    command: SetBanCommand,
    banTime: Int = 86400,
    absolute: Boolean = false
  ): ZIO[ZClient, Throwable, Unit] =
    ZIO.accessM[ZClient](
      _.zclient.bitcoindCall[Unit](
        "setban",
        List(
          JsString(address.toJavaUri.getAuthority),
          JsString(command.toString),
          JsNumber(banTime),
          JsBoolean(absolute)
        )
      )
    )

  def setExcessiveBlock(blockSize: Long): ZIO[ZClient, Throwable, String] =
    ZIO.accessM[ZClient](
      _.zclient
        .bitcoindCall[String]("setexcessiveblock", List(JsNumber(blockSize)))
    )

  def isNetworkActive: ZIO[ZClient, Throwable, Boolean] = getNetworkInfo.map(_.networkactive)

  def activateNetwork: ZIO[ZClient, Throwable, Boolean] = setNetworkActive(true)

  def deactivateNetwork: ZIO[ZClient, Throwable, Boolean] = setNetworkActive(false)

  private def setNetworkActive(activate: Boolean) =
    ZIO.accessM[ZClient](_.zclient.bitcoindCall[Boolean]("setnetworkactive", List(JsBoolean(activate))))

  private def addNode(address: URI, cmd: String): ZIO[ZClient, Throwable, Unit] =
    ZIO.accessM[ZClient](
      _.zclient.bitcoindCall[Unit]("addnode", List(JsString(address.getAuthority), JsString(cmd)))
    )

  private def getAddedNodeInfo(node: Option[URI]) = {
    val params = node.fold(List.empty[JsString])(n => List(JsString(n.getAuthority)))
    ZIO.accessM[ZClient](_.zclient.bitcoindCall[Vector[Node]]("getaddednodeinfo", params))
  }
}
