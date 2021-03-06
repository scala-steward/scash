package org.scash.rpc.client.v17

import akka.actor.ActorSystem
import org.scash.core.crypto.ECPrivateKey
import org.scash.core.protocol.transaction.Transaction
import org.scash.core.script.crypto.SigHashType
import org.scash.rpc.client.common.{ BitcoindRpcClient, BitcoindVersion, RpcOpts }
import org.scash.rpc.config.BitcoindInstance
import org.scash.rpc.jsonmodels.{ SignRawTransactionResult, TestMempoolAcceptResult }
import org.scash.rpc.serializers.JsonSerializers._
import org.scash.rpc.serializers.JsonWriters._
import org.scash.rpc.client.common.RpcOpts.SignRawTransactionOutputParameter
import org.scash.rpc.config.BitcoindInstance
import play.api.libs.json.{ JsArray, JsBoolean, JsString, Json }

import scala.concurrent.Future
import scala.util.Try

/**
 * This class is compatible with version 0.17 of Bitcoin Core.
 *
 * @see [[org.scash.rpc.client.common.BitcoindRpcClient BitcoindRpcClient Scaladocs]]
 *
 * @define signRawTx Bitcoin Core 0.17 had a breaking change in the API
 *                   for signing raw transactions. Previously the same
 *                   RPC call was used for signing a TX with existing keys
 *                   in the Bitcoin Core wallet or a manually provided private key.
 *                   These RPC calls are now separated out into two distinct calls.
 */
class BitcoindV17RpcClient(override val instance: BitcoindInstance)(
  implicit
  actorSystem: ActorSystem
) extends BitcoindRpcClient(instance)
    with V17LabelRpc
    with V17PsbtRpc {

  override def version: BitcoindVersion = BitcoindVersion.V17

  /**
   * $signRawTx
   *
   * This RPC call signs the raw transaction with keys found in
   * the Bitcoin Core wallet.
   */
  def signRawTransactionWithWallet(
    transaction: Transaction,
    utxoDeps: Vector[SignRawTransactionOutputParameter] = Vector.empty,
    sigHash: SigHashType = SigHashType.bchALL
  ): Future[SignRawTransactionResult] =
    bitcoindCall[SignRawTransactionResult](
      "signrawtransactionwithwallet",
      List(JsString(transaction.hex), Json.toJson(utxoDeps), Json.toJson(sigHash))
    )

  /**
   * $signRawTx
   *
   * This RPC call signs the raw transaction with keys provided
   * manually.
   */
  def signRawTransactionWithKey(
    transaction: Transaction,
    keys: Vector[ECPrivateKey],
    utxoDeps: Vector[RpcOpts.SignRawTransactionOutputParameter] = Vector.empty,
    sigHash: SigHashType = SigHashType.bchALL
  ): Future[SignRawTransactionResult] =
    bitcoindCall[SignRawTransactionResult](
      "signrawtransactionwithkey",
      List(JsString(transaction.hex), Json.toJson(keys), Json.toJson(utxoDeps), Json.toJson(sigHash))
    )

  // testmempoolaccept expects (and returns) a list of txes,
  // but currently only lists of length 1 is supported
  def testMempoolAccept(transaction: Transaction, allowHighFees: Boolean = false): Future[TestMempoolAcceptResult] =
    bitcoindCall[Vector[TestMempoolAcceptResult]](
      "testmempoolaccept",
      List(JsArray(Vector(Json.toJson(transaction))), JsBoolean(allowHighFees))
    ).map(_.head)
}

object BitcoindV17RpcClient {

  /**
   * Creates an RPC client from the given instance.
   *
   * Behind the scenes, we create an actor system for
   * you. You can use `withActorSystem` if you want to
   * manually specify an actor system for the RPC client.
   */
  def apply(instance: BitcoindInstance): BitcoindV17RpcClient = {
    implicit val system = ActorSystem.create(BitcoindRpcClient.ActorSystemName)
    withActorSystem(instance)
  }

  /**
   * Creates an RPC client from the given instance,
   * together with the given actor system. This is for
   * advanced users, wher you need fine grained control
   * over the RPC client.
   */
  def withActorSystem(instance: BitcoindInstance)(implicit system: ActorSystem): BitcoindV17RpcClient =
    new BitcoindV17RpcClient(instance)

  def fromUnknownVersion(rpcClient: BitcoindRpcClient): Try[BitcoindV17RpcClient] =
    Try {
      new BitcoindV17RpcClient(rpcClient.instance)(rpcClient.system)
    }
}
