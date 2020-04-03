package org.scash.zcore.number

import org.scash.zcore.typeclass.CNumeric
import scodec.Attempt.Successful
import scodec.Codec
import scodec.codecs._

case class Uint8(private[zcore] val num: Int)

object Uint8 {

  def apply(int: Int): Uint8 =
    if (isValid(int)) Uint8(int)
    else throw new IllegalArgumentException(s"$int negative or larger than 256")

  def isValid(bigInt: BigInt): Boolean = bigInt >= 0 && bigInt < 256

  implicit val uint8Codec: Codec[Uint8] = uint8L.widen(Uint8(_), u => Successful(u.num))

  implicit val uint8Numeric: CNumeric[Uint8] =
    CNumeric[Uint8](0xff)(_.num, n => Uint8(n.toInt))

}
