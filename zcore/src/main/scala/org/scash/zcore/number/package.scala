package org.scash.zcore

import org.scash.zcore.typeclass.CNumeric

import scala.math.BigInt

package object number {
  def pow2(exponent: Int): BigInt = {
    require(exponent < 64, "We cannot have anything larger than 2^64 - 1 in a long, you tried to do 2^" + exponent)
    BigInt(1) << exponent
  }

  def uLongtoBigInt(uLong: Long): BigInt =
    (BigInt(uLong >>> 1) << 1) + (uLong & 1)

  def bigIntToLong(n: BigInt): Long = {
    val smallestBit = (n & 1).toLong
    ((n >> 1).toLong << 1) | smallestBit
  }

  def verify[A: CNumeric, A1: CNumeric](bigInt: A)(min: A1, max: A1): A =
    if (bigInt >= min && bigInt <= max) bigInt
    else throw new IllegalArgumentException(s"Out of bounds: $bigInt is not between in range of: $min and $max")
}
