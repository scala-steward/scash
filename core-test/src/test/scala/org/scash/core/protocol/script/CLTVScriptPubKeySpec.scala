package org.scash.core.protocol.script

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.ScriptGenerators

/**
 * Created by tom on 8/23/16.
 */
class CLTVScriptPubKeySpec extends Properties("CLTVScriptPubKeySpec") {
  property("Serialization symmetry") =
    Prop.forAll(ScriptGenerators.cltvScriptPubKey) {
      case (cltvScriptPubKey, _) =>
        CLTVScriptPubKey(cltvScriptPubKey.hex) == cltvScriptPubKey
    }
}
