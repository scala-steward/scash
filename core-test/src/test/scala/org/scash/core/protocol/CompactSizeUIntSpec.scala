package org.scash.core.protocol

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.NumberGenerator

/**
 * Created by chris on 6/29/16.
 */
class CompactSizeUIntSpec extends Properties("CompactSizeUIntSpec") {

  property("Serialization symmetry") =
    Prop.forAll(NumberGenerator.compactSizeUInts) { compact: CompactSizeUInt =>
      CompactSizeUInt.parseCompactSizeUInt(compact.hex) == compact
    }
}
