package org.scash.rpc.zio.it

import org.scash.rpc.jsonmodels.{
  GetExcessiveBlockSize,
  GetNetTotalsResult,
  GetNetworkInfoResult,
  GetNodeAddressesResult,
  Node,
  NodeBan,
  Peer
}
import org.scash.rpc.zio.TestUtil
import org.scash.rpc.zrpc.zrpc

import zio.test.Assertion._
import zio.test._

object NetworkRPCTest
    extends DefaultRunnableSpec(
      suite("BlockchainRPC")(
        testM("Remove and Add Node from addnode list") {
          val test = for {
            nodes <- zrpc.getAllAddedNodes
            _     <- zrpc.removeNode(nodes.head.addednode)
            a     <- zrpc.addNode(nodes.head.addednode)
          } yield assert(a, isUnit)
          test.provideManaged(TestUtil.instance)
        },
        testM("ClearBanned") {
          val test = assertM(zrpc.clearBanned, isUnit)
          test.provideManaged(TestUtil.instance)
        },
        testM("DisconnectNode") {
          val test = for {
            peer <- zrpc.getPeerInfo
            ans  <- zrpc.disconnectNode(peer.head.id)
          } yield assert(ans, isUnit)
          test.provideManaged(TestUtil.instance)
        },
        testM("GetAddedNodeInfo") {
          val test = assertM(zrpc.getAllAddedNodes, isSubtype[Vector[Node]](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        /* TODO: this is a bug in ZIO. its fixed in RC18 and ill uncomment then
        testM("GetConnectionCount") {
          val test = assertM(zrpc.getConnectionCount, isSubtype[Int](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
         */
        testM("GetExcessiveBlock") {
          val test = assertM(zrpc.getExcessiveBlock, isSubtype[GetExcessiveBlockSize](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("GetNetTotals") {
          val test = assertM(zrpc.getNetTotals, isSubtype[GetNetTotalsResult](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("GetNetworkInfo") {
          val test = assertM(zrpc.getNetworkInfo, isSubtype[GetNetworkInfoResult](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("isNetworkActive") {
          val test = assertM(zrpc.isNetworkActive, isTrue)
          test.provideManaged(TestUtil.instance)
        },
        testM("GetNodeAddresses") {
          val test = assertM(zrpc.getNodeAddresses(1), isSubtype[Vector[GetNodeAddressesResult]](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("GetPeerInfo") {
          val test = assertM(zrpc.getPeerInfo, isSubtype[Vector[Peer]](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("Listbanned") {
          val test = assertM(zrpc.listBanned, isSubtype[Vector[NodeBan]](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        testM("Ping") {
          val test = assertM(zrpc.ping, isUnit)
          test.provideManaged(TestUtil.instance)
        },
        testM("SetExcessiveBlock") {
          val test = assertM(zrpc.setExcessiveBlock(32000000L), isSubtype[String](Assertion.anything))
          test.provideManaged(TestUtil.instance)
        },
        suite("SetNetworkActive")(
          testM("ActivateNetwork") {
            val test = for {
              act  <- zrpc.activateNetwork
              bool <- zrpc.isNetworkActive
            } yield assert(bool && act, isTrue)
            test.provideManaged(TestUtil.instance)
          },
          testM("DeactivateNetwork") {
            val test = for {
              dis <- zrpc.deactivateNetwork
              con <- zrpc.activateNetwork
            } yield assert(!dis && con, isTrue)
            test.provideManaged(TestUtil.instance)
          }
        )
      ) @@ TestAspect.sequential
    )
