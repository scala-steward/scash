package org.scash.zcore.number

import scala.math.BigInt

private[scash] object NumericUtil {
  def pow2(exponent: Int): BigInt = {
    require(exponent < 64, "We cannot have anything larger than 2^64 - 1 in a long, you tried to do 2^" + exponent)
    BigInt(1) << exponent
  }

  def longToBigInt(unsignedLong: Long): BigInt =
    (BigInt(unsignedLong >>> 1) << 1) + (unsignedLong & 1)

  def bigIntToLong(n: BigInt): Long = {
    val smallestBit = (n & 1).toLong
    ((n >> 1).toLong << 1) | smallestBit
  }
}
