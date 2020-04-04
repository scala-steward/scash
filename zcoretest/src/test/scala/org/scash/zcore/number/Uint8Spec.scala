package org.scash.zcore.number

import org.scash.zcore._
import org.scash.ztestkit.gen.UintGen

import zio.test.Assertion.equalTo
import zio.test._

object Uint8Spec extends DefaultRunnableSpec {
  val spec = suite("Uint8Spec")(
    testM("uint8 -> byte -> uint8")(
      check(UintGen.uint8)(u => assert(u.bytes.decode[Uint8])(equalTo(u)))
    )
  )
}
/*
  property("serialization symmetry") = {
    Prop.forAll(NumberGenerator.uInt8) { u8 =>
      UInt8(u8.hex) == u8
    }
  }

  property("<<") = {
    Prop.forAllNoShrink(NumberGenerator.uInt8, Gen.choose(0, 8)) {
      case (u8: UInt8, shift: Int) =>
        val r = Try(u8 << shift)
        val expected = (u8.toLong << shift) & 0xffL
        if (expected <= UInt8.max.toLong) {
          r.get == UInt8(expected.toShort)
        } else {
          r.isFailure
        }
    }
  }

  property(">>") = {
    Prop.forAllNoShrink(NumberGenerator.uInt8, Gen.choose(0, 100)) {
      case (u8: UInt8, shift: Int) =>
        val r = (u8 >> shift)
        val expected = if (shift > 31) UInt8.zero else UInt8((u8.toLong >> shift).toShort)
        if (r != expected) {
          logger.warn("expected: " + expected)
          logger.warn("r: " + r)
        }
        r == expected

    }
  }
}
 */
