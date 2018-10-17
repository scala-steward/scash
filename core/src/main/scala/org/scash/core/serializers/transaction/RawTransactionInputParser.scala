package org.scash.core.serializers.transaction

import org.scash.core.protocol.script.ScriptSignature
import org.scash.core.protocol.transaction.{ TransactionInput, TransactionOutPoint }
import org.scash.core.serializers.script.RawScriptSignatureParser
import org.scash.core.number.UInt32
import org.scash.core.serializers.RawBitcoinSerializer
import org.scash.core.util.BitcoinSUtil
import scodec.bits.ByteVector

/**
 * Created by chris on 1/13/16.
 * https://bitcoin.org/en/developer-reference#txin
 */
sealed abstract class RawTransactionInputParser extends RawBitcoinSerializer[TransactionInput] {

  override def read(bytes: ByteVector): TransactionInput = {
    val outPoint = TransactionOutPoint(bytes.take(36))
    val scriptSigBytes = bytes.slice(36, bytes.size)
    val scriptSig: ScriptSignature = RawScriptSignatureParser.read(scriptSigBytes)
    val endOfScriptSigBytes = 36 + scriptSig.bytes.size
    val lastInputByte = endOfScriptSigBytes + 4
    val sequenceBytes = bytes.slice(endOfScriptSigBytes, lastInputByte)
    val sequenceNumber: UInt32 = UInt32(sequenceBytes.reverse)
    val txInput = TransactionInput(outPoint, scriptSig, sequenceNumber)
    txInput
  }

  /** Writes a single transaction input */
  def write(input: TransactionInput): ByteVector = {
    input.previousOutput.bytes ++ input.scriptSignature.bytes ++ input.sequence.bytes.reverse
  }
}

object RawTransactionInputParser extends RawTransactionInputParser
