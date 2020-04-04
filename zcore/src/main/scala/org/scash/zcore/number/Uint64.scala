package org.scash.zcore.number

import org.scash.zcore.typeclass.CNumeric

import scodec.Codec
import scodec.codecs._

protected case class Uint64(num: BigInt) extends AnyVal

object Uint64 {

  def apply(bigInt: BigInt): Uint64 = new Uint64(verify(bigInt)(min, max))

  lazy val min = Uint64(BigInt(0))
  lazy val max = Uint64(BigInt("18446744073709551615"))

  implicit val uint64L: Codec[Uint64] = int64L.xmap(
    l => apply(uLongtoBigInt(l)),
    n => bigIntToLong(n.num)
  )

  implicit val uint64Numeric: CNumeric[Uint64] =
    CNumeric[Uint64](0xFFFFFFFFFFFFFFFFL)(_.num, apply(_))

}
