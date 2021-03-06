package org.scash.core.protocol.script

import org.scash.core.gen.CryptoGenerators
import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.{CryptoGenerators, ScriptGenerators}

/**
 * Created by chris on 6/22/16.
 */
class P2PKHScriptPubKeySpec extends Properties("P2PKHScriptPubKeySpec") {

  property("Serialization symmetry") =
    Prop.forAll(ScriptGenerators.p2pkhScriptPubKey) {
      case (p2pkhScriptPubKey, _) =>
        P2PKHScriptPubKey(p2pkhScriptPubKey.hex) == p2pkhScriptPubKey
    }

  property("find pubkeyhash in scriptPubKey") =
    Prop.forAll(CryptoGenerators.sha256Hash160Digest) { hash =>
      val scriptPubKey = P2PKHScriptPubKey(hash)
      scriptPubKey.pubKeyHash == hash
    }
}
