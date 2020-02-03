package org.scash.rpc.common

import org.scash.core.crypto.ECPrivateKey
import org.scash.core.protocol.P2PKHAddress
import org.scash.rpc.client.common.BitcoindRpcClient
import org.scash.rpc.client.common.RpcOpts.AddressType
import org.scash.testkit.rpc.BitcoindRpcTestUtil
import org.scash.testkit.util.BitcoindRpcTest

import scala.concurrent.Future

class MultisigRpcTest extends BitcoindRpcTest {
  lazy val clientF: Future[BitcoindRpcClient] =
    BitcoindRpcTestUtil.startedBitcoindRpcClient(clientAccum = clientAccum)

  behavior of "MultisigRpc"

  it should "be able to create a multi sig address" in {
    val ecPrivKey1 = ECPrivateKey.freshPrivateKey
    val ecPrivKey2 = ECPrivateKey.freshPrivateKey

    val pubKey1 = ecPrivKey1.publicKey
    val pubKey2 = ecPrivKey2.publicKey

    for {
      client <- clientF
      _ <- client.createMultiSig(2, Vector(pubKey1, pubKey2))
    } yield succeed
  }

  it should "be able to add a multi sig address to the wallet" in {
    val ecPrivKey1 = ECPrivateKey.freshPrivateKey
    val pubKey1 = ecPrivKey1.publicKey

    for {
      client <- clientF
      address <- client.getNewAddress(addressType = AddressType.Legacy)
      _ <- {
        val pubkey = Left(pubKey1)
        val p2pkh = Right(address.asInstanceOf[P2PKHAddress])
        client
          .addMultiSigAddress(2, Vector(pubkey, p2pkh))
      }
    } yield succeed
  }

}
