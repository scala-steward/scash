package org.scash.zcore.number

import org.scash.zcore.typeclass.CNumeric
import scodec.Codec
import scodec.codecs._

import scala.util.Try

case class Uint8(num: Int) extends AnyVal

object Uint8 {

  def apply(int: Int): Uint8 = Uint8(verify(int)(min, max))

  def safe(int: Int): Option[Uint8] = Try(apply(int)).toOption

  val min = Uint8(0)
  val one = Uint8(1)
  val max = Uint8(255)

  implicit val uint8Codec: Codec[Uint8] = uint8L.xmap(apply(_), _.num)

  implicit val uint8Numeric: CNumeric[Uint8] =
    CNumeric[Uint8](0xFF)(_.num, n => apply(n.toInt))

}
