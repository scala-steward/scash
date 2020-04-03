package org.scash.zcore.number

import org.scash.zcore.number.NumericUtil
import org.scash.zcore.typeclass.CNumeric
import scodec.Attempt.Successful
import scodec.Codec
import scodec.codecs._

case class Uint32(private[zcore] val num: BigInt)

object Uint32 {

  implicit val uint32Codec: Codec[Uint32] = uint32L.widen(
    l => Uint32(NumericUtil.longToBigInt(l)),
    u => Successful(NumericUtil.bigIntToLong(u.num))
  )

  implicit val uint32Numeric: CNumeric[Uint32] =
    CNumeric[Uint32](0xFFFFFFFFL)(_.num, Uint32(_))

}
