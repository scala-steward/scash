package org.scash.core.protocol.script

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.ScriptGenerators

/**
 * Created by chris on 6/24/16.
 */
class P2SHScriptPubKeySpec extends Properties("P2SHScriptPubKeySpec") {

  property("Symmetrical serialization") =
    Prop.forAll(ScriptGenerators.p2shScriptPubKey) {
      case (p2shScriptPubKey, _) =>
        P2SHScriptPubKey(p2shScriptPubKey.hex) == p2shScriptPubKey
    }
}
