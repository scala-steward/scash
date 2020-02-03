package org.scash.core.protocol.script

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.ScriptGenerators

/**
 * Created by chris on 3/27/17.
 */
class EscrowTimeoutSpec extends Properties("CSVEscrowWithTimeoutSpec") {

  property("serialization symmetry") = {
    Prop.forAll(ScriptGenerators.escrowTimeoutScriptPubKey) {
      case (escrowTimeout, _) =>
        EscrowTimeoutScriptPubKey(escrowTimeout.hex) == escrowTimeout &&
          EscrowTimeoutScriptPubKey(escrowTimeout.escrow, escrowTimeout.timeout) == escrowTimeout &&
          EscrowTimeoutScriptPubKey.isValidEscrowTimeout(escrowTimeout.asm)
    }
  }
}
