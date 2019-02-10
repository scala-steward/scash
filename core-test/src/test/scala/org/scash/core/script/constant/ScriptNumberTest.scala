package org.scash.core.script.constant

import org.scalatest.{ FlatSpec, MustMatchers }
import org.scash.core.number.Int64

import scala.util.Try

/**
 * Created by chris on 1/25/16.
 */
class ScriptNumberTest extends FlatSpec with MustMatchers {

  val zero = ScriptNumber.zero
  val one = ScriptNumber.one
  val ten = ScriptNumber(10)

  "ScriptNumber" must "derive the correct hex value from a script number" in {
    ScriptNumber(1).hex must be("01")
    ScriptNumber(8).hex must be("08")
  }

  it must "add two script numbers correctly" in {
    (zero + zero) must be(zero)
    (one + zero) must be(one)
    (one + ten) must be(ScriptNumber(11))
  }

  it must "subtract two script numbers correctly" in {
    (zero - zero) must equal(zero)
    (one - zero) must equal(one)
    (ten - one) must equal(ScriptNumber(9))
  }

  it must "multiply two script numbers correctly" in {
    (zero * zero) must equal(zero)
    (one * zero) must equal(zero)
    (ten * one) must equal(ten)
    (ten * ScriptNumber(5)) must equal(ScriptNumber(50))
  }

  it must "divide two script numbers correctly" in {
    Try((zero / zero)).isFailure must be(true)
    Try((one / zero)).isFailure must be(true)
    (zero / one) must be(zero)
    (ScriptNumber(100) / ScriptNumber(10)) must equal(ten)
    (ten / ScriptNumber(3)) must equal(ScriptNumber(3))
  }

  it must "compare ScriptNumbers to Int64 correctly" in {
    (zero < Int64.one) must equal(true)
    (zero <= Int64.zero) must equal(true)
    (one > Int64.zero) must equal(true)
    (one >= Int64.one) must equal(true)
  }

  it must "compute bitwise operations correctly" in {
    (one & Int64.one).toInt must be(1)
    (one & one).toInt must be(1)
    (one | one).toInt must be(1)
    (one % one).toInt must be(0)
    (one % ScriptNumber(3)).toInt must be(1)
    Try(one % zero).isFailure must be(true)
  }
}
