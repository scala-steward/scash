package org.scash.core.protocol.script

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.ScriptGenerators

/**
 * Created by chris on 6/22/16.
 */
class MultiSignatureScriptPubKeySpec extends Properties("MultiSignatureScriptPubKeySpec") {

  property("Serialization symmetry") =
    Prop.forAll(ScriptGenerators.multiSigScriptPubKey) {
      case (multiSigScriptPubKey, _) =>
        MultiSignatureScriptPubKey(multiSigScriptPubKey.hex) == multiSigScriptPubKey

    }
}
