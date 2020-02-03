package org.scash.rpc.client.common

import org.scash.core.crypto.ECPublicKey
import org.scash.core.protocol.P2PKHAddress
import org.scash.rpc.client.common.RpcOpts.AddressType
import org.scash.rpc.jsonmodels.MultiSigResult
import org.scash.rpc.serializers.JsonSerializers._
import org.scash.rpc.serializers.JsonWriters._
import play.api.libs.json.{JsArray, JsNumber, JsString, Json}

import scala.concurrent.Future

/**
  * This trait defines RPC calls related to
  * multisignature functionality in Bitcoin Core.
  *
  * @see [[https://en.bitcoin.it/wiki/Multisignature Bitcoin Wiki]]
  *     article on multisignature.
  */
trait MultisigRpc { self: Client =>

  private def addMultiSigAddress(
      minSignatures: Int,
      keys: Vector[Either[ECPublicKey, P2PKHAddress]],
      account: String = "",
      addressType: Option[AddressType]): Future[MultiSigResult] = {
    def keyToString(key: Either[ECPublicKey, P2PKHAddress]): JsString =
      key match {
        case Right(k) => JsString(k.value)
        case Left(k)  => JsString(k.hex)
      }

    val params =
      List(JsNumber(minSignatures),
           JsArray(keys.map(keyToString)),
           JsString(account)) ++ addressType.map(Json.toJson(_)).toList

    bitcoindCall[MultiSigResult]("addmultisigaddress", params)
  }

  def addMultiSigAddress(
      minSignatures: Int,
      keys: Vector[Either[ECPublicKey, P2PKHAddress]]): Future[MultiSigResult] =
    addMultiSigAddress(minSignatures, keys, addressType = None)

  def addMultiSigAddress(
      minSignatures: Int,
      keys: Vector[Either[ECPublicKey, P2PKHAddress]],
      account: String): Future[MultiSigResult] =
    addMultiSigAddress(minSignatures, keys, account, None)

  def addMultiSigAddress(
      minSignatures: Int,
      keys: Vector[Either[ECPublicKey, P2PKHAddress]],
      addressType: AddressType): Future[MultiSigResult] =
    addMultiSigAddress(minSignatures, keys, addressType = Some(addressType))

  def addMultiSigAddress(
      minSignatures: Int,
      keys: Vector[Either[ECPublicKey, P2PKHAddress]],
      account: String,
      addressType: AddressType): Future[MultiSigResult] =
    addMultiSigAddress(minSignatures, keys, account, Some(addressType))

  def createMultiSig(
      minSignatures: Int,
      keys: Vector[ECPublicKey]): Future[MultiSigResult] = {
    bitcoindCall[MultiSigResult](
      "createmultisig",
      List(JsNumber(minSignatures), Json.toJson(keys.map(_.hex))))
  }

}
