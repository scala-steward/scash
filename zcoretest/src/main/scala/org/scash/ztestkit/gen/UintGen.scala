package org.scash.ztestkit.gen

import org.scash.zcore.number.{ Uint32, Uint8 }
import org.scash.zcore.number._
import zio.test._

object UintGen {

  /** Creates a generator that generates positive long numbers */
  def positiveLongs = Gen.long(0, Long.MaxValue)

  /** Creates a generator for positive longs without the number zero */
  def positiveLongsNoZero = Gen.long(1, Long.MaxValue)

  /** Creates a number generator that generates negative long numbers */
  def negativeLongs = Gen.long(Long.MinValue, -1)

  /** Generates a random uint8 */
  def uint8 = Gen.int(Uint8.min.num, Uint8.max.num).map(Uint8(_))

  /** Generates a random uint32 */
  def uint32 = Gen.long(Uint32.min.num, Uint32.max.num).map(Uint32(_))

  /** Generates a random uint32 */
  def uint64 = positiveBigInts.filter(_ < (BigInt(1) << 64)).map(Uint64(_))

  /** Chooses a BigInt in the ranges of 0 <= bigInt < 2^^64 */
  def bigInts = Gen.long(Long.MinValue, Long.MaxValue).map(n => BigInt(n) + BigInt(2).pow(63))

  def positiveBigInts = bigInts.filter(_ >= 0)
  /**
   * Generates a number in the range 0 <= x < 2^^64
   * then wraps it in a UInt64
   */
  /*
  def uInt64s: Gen[UInt64] =
    for {
      bigInt <- bigIntsUInt64Range
    } yield UInt64(bigInt)

  def int32s: Gen[Int32] = Gen.choose(Int32.min.toLong, Int32.max.toLong).map(Int32(_))

  def int64s: Gen[Int64] = Gen.choose(Int64.min.toLong, Int64.max.toLong).map(Int64(_))

  def scriptNumbers: Gen[ScriptNumber] = Gen.choose(Int64.min.toLong, Int64.max.toLong).map(ScriptNumber(_))

  def positiveScriptNumbers: Gen[ScriptNumber] = Gen.choose(0L, Int64.max.toLong).map(ScriptNumber(_))

  def compactSizeUInts: Gen[CompactSizeUInt] = uInt64s.map(CompactSizeUInt(_))

  /** Generates an arbitrary [[Byte]] in Scala */
  def byte: Gen[Byte] = arbitrary[Byte]

  /** Generates a 100 byte sequence */
  def bytes: Gen[List[Byte]] =
    for {
      num <- Gen.choose(0, 100)
      b   <- bytes(num)
    } yield b

  /**
 * Generates the number of bytes specified by num
 * @param num
 * @return
 */
  def bytes(num: Int): Gen[List[Byte]] = Gen.listOfN(num, byte)

  /** Generates a random boolean */
  def bool: Gen[Boolean] =
    for {
      num <- Gen.choose(0, 1)
    } yield num == 1

  /** Generates a bit vector */
  def bitVector: Gen[BitVector] =
    for {
      n      <- Gen.choose(0, 100)
      vector <- Gen.listOfN(n, bool)
    } yield BitVector.bits(vector)
 */
}
