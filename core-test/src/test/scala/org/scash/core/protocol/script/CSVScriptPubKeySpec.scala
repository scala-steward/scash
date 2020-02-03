package org.scash.core.protocol.script

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.ScriptGenerators

/**
 * Created by tom on 8/23/16.
 */
class CSVScriptPubKeySpec extends Properties("CSVScriptPubKeySpec") {
  property("Serialization Symmetry") =
    Prop.forAll(ScriptGenerators.csvScriptPubKey) {
      case (csvScriptPubKey, _) =>
        CSVScriptPubKey(csvScriptPubKey.hex) == csvScriptPubKey
    }
}
