package org.scash.rpc.serializers

import org.scash.core.crypto.{ DoubleSha256Digest, DoubleSha256DigestBE }
import org.scash.core.currency.Bitcoins
import org.scash.core.number.UInt32
import org.scash.core.protocol.BitcoinAddress
import org.scash.core.protocol.script.ScriptPubKey
import org.scash.core.protocol.transaction.{ Transaction, TransactionInput }
import org.scash.core.script.crypto._
import org.scash.core.util.BitcoinSUtil
import org.scash.rpc.client.common.RpcOpts.{ AddressType, WalletCreateFundedPsbtOptions }
import play.api.libs.json._

import scala.collection.mutable

// for mapWrites below
import scala.language.implicitConversions

object JsonWriters {
  implicit object HashTypeWrites extends Writes[SigHashType] {
    override def writes(hash: SigHashType): JsValue = hash match {
      case SigHashType.bchALL                => JsString("ALL")
      case SigHashType.bchNONE               => JsString("NONE")
      case SigHashType.bchSINGLE             => JsString("SINGLE")
      case SigHashType.bchALLANYONECANPAY    => JsString("ALL|ANYONECANPAY")
      case SigHashType.bchNONEANYONECANPAY   => JsString("NONE|ANYONECANPAY")
      case SigHashType.bchSINGLEANYONECANPAY => JsString("SINGLE|ANYONECANPAY")
      case _ =>
        throw new IllegalArgumentException("SIGHHASH_ANYONECANPAY is not supported by the bitcoind RPC interface")
    }
  }

  implicit object BitcoinsWrites extends Writes[Bitcoins] {
    override def writes(o: Bitcoins): JsValue = JsNumber(o.toBigDecimal)
  }

  implicit object BitcoinAddressWrites extends Writes[BitcoinAddress] {
    override def writes(o: BitcoinAddress): JsValue = JsString(o.value)
  }

  implicit object DoubleSha256DigestWrites extends Writes[DoubleSha256Digest] {
    override def writes(o: DoubleSha256Digest): JsValue = JsString(o.hex)
  }

  implicit object DoubleSha256DigestBEWrites extends Writes[DoubleSha256DigestBE] {
    override def writes(o: DoubleSha256DigestBE): JsValue = JsString(o.hex)
  }

  implicit object ScriptPubKeyWrites extends Writes[ScriptPubKey] {
    override def writes(o: ScriptPubKey): JsValue =
      JsString(BitcoinSUtil.encodeHex(o.asmBytes))
  }

  implicit object TransactionInputWrites extends Writes[TransactionInput] {
    override def writes(o: TransactionInput): JsValue =
      JsObject(
        Seq(
          ("txid", JsString(o.previousOutput.txIdBE.hex)),
          ("vout", JsNumber(o.previousOutput.vout.toLong)),
          ("sequence", JsNumber(o.sequence.toLong))
        )
      )
  }

  implicit object UInt32Writes extends Writes[UInt32] {
    override def writes(o: UInt32): JsValue = JsNumber(o.toLong)
  }

  implicit object TransactionWrites extends Writes[Transaction] {
    override def writes(o: Transaction): JsValue = JsString(o.hex)
  }

  implicit def mapWrites[K, V](keyString: K => String)(
    implicit
    vWrites: Writes[V]
  ): Writes[Map[K, V]] =
    new Writes[Map[K, V]] {
      override def writes(o: Map[K, V]): JsValue =
        Json.toJson(o.map { case (k, v) => (keyString(k), v) })
    }

  implicit object AddressTypeWrites extends Writes[AddressType] {
    override def writes(addr: AddressType): JsValue = JsString(addr.toString)
  }

  implicit object WalletCreateFundedPsbtOptionsWrites extends Writes[WalletCreateFundedPsbtOptions] {
    override def writes(opts: WalletCreateFundedPsbtOptions): JsValue = {
      val jsOpts: mutable.Map[String, JsValue] = mutable.Map(
        "includeWatching" -> JsBoolean(opts.includeWatching),
        "lockUnspents"    -> JsBoolean(opts.lockUnspents),
        "replaceable"     -> JsBoolean(opts.replaceable),
        "estimate_mode"   -> JsString(opts.estimateMode.toString)
      )

      def addToMapIfDefined[T](key: String, opt: Option[T])(implicit writes: Writes[T]): Unit =
        opt.foreach(o => jsOpts += (key -> Json.toJson(o)))

      addToMapIfDefined("changeAddress", opts.changeAddress)
      addToMapIfDefined("changePosition", opts.changePosition)
      addToMapIfDefined("change_type", opts.changeType)
      addToMapIfDefined("feeRate", opts.feeRate)
      addToMapIfDefined("subtractFeeFromOutputs", opts.subtractFeeFromOutputs)
      addToMapIfDefined("conf_target", opts.confTarget)

      JsObject(jsOpts)
    }
  }
}
