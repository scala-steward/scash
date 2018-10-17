package org.scash.rpc.client

import org.scash.core.crypto.{DoubleSha256Digest, ECPrivateKey}
import org.scash.core.currency.Bitcoins
import org.scash.core.number.UInt32
import org.scash.core.protocol.BitcoinAddress
import org.scash.core.protocol.script.ScriptPubKey
import play.api.libs.json.{Json, Writes}

object RpcOpts {
  case class FundRawTransactionOptions(
    changeAddress: Option[BitcoinAddress] = None,
    changePosition: Option[Int] = None,
    includeWatching: Boolean = false,
    lockUnspents: Boolean = false,
    reverseChangeKey: Boolean = true,
    feeRate: Option[Bitcoins] = None,
    subtractFeeFromOutputs: Option[Array[Int]])

  implicit val fundRawTransactionOptionsWrites: Writes[FundRawTransactionOptions] = Json.writes[FundRawTransactionOptions]

  case class SignRawTransactionOutputParameter(
    txid: DoubleSha256Digest,
    vout: Int,
    scriptPubKey: ScriptPubKey,
    redeemScript: Option[ScriptPubKey] = None,
    amount: Bitcoins)

  implicit val signRawTransactionOutputParameterWrites: Writes[SignRawTransactionOutputParameter] =
    Json.writes[SignRawTransactionOutputParameter]

  case class ImportMultiRequest(
    scriptPubKey: ImportMultiAddress,
    timestamp: UInt32,
    redeemscript: Option[ScriptPubKey] = None,
    pubkeys: Option[Vector[ScriptPubKey]] = None,
    keys: Option[Vector[ECPrivateKey]] = None,
    internal: Option[Boolean] = None,
    watchonly: Option[Boolean] = None,
    label: Option[String] = None)

  case class ImportMultiAddress(address: BitcoinAddress)

  case class LockUnspentOutputParameter(txid: DoubleSha256Digest, vout: Int)

  implicit val lockUnspentParameterWrites: Writes[LockUnspentOutputParameter] =
    Json.writes[LockUnspentOutputParameter]

  sealed abstract class AddressType

  case class Legacy() extends AddressType

  def addressTypeString(addressType: AddressType): String = addressType match {
    case Legacy() => "legacy"
  }

  case class BlockTemplateRequest(
    mode: String,
    capabilities: Vector[String],
    rules: Vector[String])

  implicit val blockTemplateRequest: Writes[BlockTemplateRequest] =
    Json.writes[BlockTemplateRequest]
}
