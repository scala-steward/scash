package org.scash.core.script.crypto

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.NumberGenerator

class SigHashTypeSpec extends Properties("SigHashTypeSpec") {

  property("serialization symmetry") = {
    Prop.forAll(NumberGenerator.uInt32s) { ui32 =>
      val hashType = SigHashType.decode(ui32)
      hashType.serialize.head == ui32.bytes.last &&
        ui32.bytes.last == hashType.byte &&
        SigHashType.fromByte(hashType.byte).byte == hashType.byte

    }
  }
}
