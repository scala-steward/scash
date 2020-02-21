package org.scash.core.serializers.p2p.messages

import org.scash.core.protocol.transaction.Transaction
import org.scash.core.serializers.RawBitcoinSerializer
import org.scash.core.p2p.TransactionMessage
import scodec.bits.ByteVector

/**
 * Responsible for serializing and deserializing TransactionMessage network objects
 * @see https://bitcoin.org/en/developer-reference#tx
 */
trait RawTransactionMessageSerializer extends RawBitcoinSerializer[TransactionMessage] {

  def read(bytes: ByteVector): TransactionMessage = {
    val transaction = Transaction(bytes)
    TransactionMessage(transaction)
  }

  def write(transactionMessage: TransactionMessage): ByteVector =
    transactionMessage.transaction.bytes
}

object RawTransactionMessageSerializer extends RawTransactionMessageSerializer
