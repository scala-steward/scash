package org.scash.core.protocol.script

import org.scash.core.script.constant._
import scodec.bits.ByteVector

/**
 * Created by chris on 1/19/16.
 */
sealed trait ScriptPubKeyUpdateIndicator
case class UpdateScriptPubKeyAsm(asm: Seq[ScriptToken]) extends ScriptPubKeyUpdateIndicator
case class UpdateScriptPubKeyBytes(bytes: ByteVector)   extends ScriptPubKeyUpdateIndicator
