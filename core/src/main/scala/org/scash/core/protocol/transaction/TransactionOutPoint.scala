package org.scash.core.protocol.transaction

import org.scash.core.crypto.{ DoubleSha256Digest, DoubleSha256DigestBE }
import org.scash.core.number.UInt32
import org.scash.core.protocol.NetworkElement
import org.scash.core.serializers.transaction.RawTransactionOutPointParser
import org.scash.core.util.{ BitcoinSUtil, Factory }
import scodec.bits.ByteVector

/**
 * Created by chris on 12/26/15.
 *
 */
sealed abstract class TransactionOutPoint extends NetworkElement {

  /** The transaction id for the crediting transaction for this input */
  def txId: DoubleSha256Digest

  def txIdBE: DoubleSha256DigestBE = txId.flip

  /** The output index in the parent transaction for the output we are spending */
  def vout: UInt32

  override def bytes = RawTransactionOutPointParser.write(this)
}

/**
 * UInt32s cannot hold negative numbers, but sometimes the Bitcoin Protocol requires the vout to be -1, which is serialized
 * as "0xFFFFFFFF".
 * https://github.com/bitcoin/bitcoin/blob/d612837814020ae832499d18e6ee5eb919a87907/src/primitives/transaction.h
 * http://stackoverflow.com/questions/2711522/what-happens-if-i-assign-a-negative-value-to-an-unsigned-variable
 */
case object EmptyTransactionOutPoint extends TransactionOutPoint {
  def txId =
    DoubleSha256Digest(BitcoinSUtil.decodeHex("0000000000000000000000000000000000000000000000000000000000000000"))
  def vout = UInt32("ffffffff")
}

object TransactionOutPoint extends Factory[TransactionOutPoint] {

  private case class TransactionOutPointImpl(txId: DoubleSha256Digest, vout: UInt32) extends TransactionOutPoint

  def fromBytes(bytes: ByteVector): TransactionOutPoint = RawTransactionOutPointParser.read(bytes)

  def apply(txId: DoubleSha256Digest, index: UInt32): TransactionOutPoint =
    if (txId == EmptyTransactionOutPoint.txId && index == EmptyTransactionOutPoint.vout) {
      EmptyTransactionOutPoint
    } else TransactionOutPointImpl(txId, index)

  /**
   * @param The transaction id for the crediting transaction for this input
   * @param vout The output index in the parent transaction for the output we are spending
   */
  def apply(txId: DoubleSha256DigestBE, vout: UInt32): TransactionOutPoint =
    TransactionOutPoint(txId.flip, vout)
}
