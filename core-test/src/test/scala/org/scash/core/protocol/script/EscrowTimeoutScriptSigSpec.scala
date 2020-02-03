package org.scash.core.protocol.script

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.ScriptGenerators

/**
 * Created by chris on 3/28/17.
 */
class EscrowTimeoutScriptSigSpec extends Properties("EscrowWithTimeoutScriptSigSpec") {

  property("serialization symmetry") =
    Prop.forAll(ScriptGenerators.escrowTimeoutScriptSig) { scriptSig =>
      EscrowTimeoutScriptSignature(scriptSig.hex) == scriptSig &&
        EscrowTimeoutScriptSignature.fromAsm(scriptSig.asm) == scriptSig
    }
}
