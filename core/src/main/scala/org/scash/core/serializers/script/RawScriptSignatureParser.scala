package org.scash.core.serializers.script

import org.scash.core.protocol.CompactSizeUInt
import org.scash.core.protocol.script._
import org.scash.core.script.constant.ScriptToken
import org.scash.core.serializers.RawBitcoinSerializer

import scodec.bits.ByteVector

/**
 * Created by chris on 1/12/16.
 */
sealed abstract class RawScriptSignatureParser extends RawBitcoinSerializer[ScriptSignature] {

  def read(bytes: ByteVector): ScriptSignature =
    if (bytes.isEmpty) EmptyScriptSignature
    else {
      val compactSizeUInt = CompactSizeUInt.parseCompactSizeUInt(bytes)
      //TODO: Figure out a better way to do this, we can theoretically have numbers larger than Int.MaxValue,
      val scriptSigBytes =
        bytes.slice(compactSizeUInt.size.toInt, compactSizeUInt.num.toInt + compactSizeUInt.size.toInt)
      val scriptTokens: List[ScriptToken] = ScriptParser.fromBytes(scriptSigBytes)
      ScriptSignature.fromAsm(scriptTokens)
    }

  def write(scriptSig: ScriptSignature): ByteVector = scriptSig.bytes
}

object RawScriptSignatureParser extends RawScriptSignatureParser
