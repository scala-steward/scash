package org.scash.core.protocol.script

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.ScriptGenerators

/**
 * Created by chris on 6/22/16.
 */
class MultiSignatureScriptSignatureSpec extends Properties("MultiSignatureScriptSigSpec") {

  property("Serialization symmetry") =
    Prop.forAll(ScriptGenerators.multiSignatureScriptSignature) { multiSigScriptSig =>
      MultiSignatureScriptSignature(multiSigScriptSig.hex) == multiSigScriptSig

    }
}
