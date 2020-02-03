package org.scash.core.protocol.script

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.ScriptGenerators

class ScriptSpec extends Properties("ScriptSpec") {

  property("serialization symmetry for ScriptFactory.fromAsmBytes") = {
    Prop.forAllNoShrink(ScriptGenerators.scriptPubKey) {
      case (spk, _) =>
        ScriptPubKey.fromAsmBytes(spk.asmBytes) == spk
    }
  }

  property("serialization symmetry for ScriptFactory.fromAsmBytes") = {
    Prop.forAllNoShrink(ScriptGenerators.scriptSignature) {
      case ss =>
        ScriptSignature.fromAsmBytes(ss.asmBytes) == ss
    }
  }
}
