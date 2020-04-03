package org.scash.core.number

private[core] object NumericUtil {
  def longToBigInt(unsignedLong: Long): BigInt =
    (BigInt(unsignedLong >>> 1) << 1) + (unsignedLong & 1)

  def bigIntToLong(n: BigInt): Long = {
    val smallestBit = (n & 1).toLong
    ((n >> 1).toLong << 1) | smallestBit
  }
}
