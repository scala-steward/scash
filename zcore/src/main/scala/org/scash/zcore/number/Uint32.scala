package org.scash.zcore.number

import org.scash.zcore.typeclass.CNumeric
import scodec.Codec
import scodec.codecs._

import scala.util.Try

protected case class Uint32(num: Long) extends AnyVal

object Uint32 {
  def apply(long: Long): Uint32 = new Uint32(verify(long)(min, max))

  def safe(long: Long): Option[Uint32] = Try(apply(long)).toOption

  val min = Uint32(0)
  val max = Uint32(4294967295L)

  implicit val uint32Codec: Codec[Uint32] = uint32L.xmap[Uint32](apply(_), _.num)

  implicit val uint32Numeric: CNumeric[Uint32] =
    CNumeric[Uint32](0xFFFFFFFFL)(_.num, l => apply(l.toLong))
}
