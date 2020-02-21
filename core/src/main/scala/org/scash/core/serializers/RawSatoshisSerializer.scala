package org.scash.core.serializers

import org.scash.core.currency.Satoshis
import org.scash.core.number
import org.scash.core.number.Int64
import scodec.bits.ByteVector

/**
 * Created by chris on 6/23/16.
 */
trait RawSatoshisSerializer extends RawBitcoinSerializer[Satoshis] {

  def read(bytes: ByteVector): Satoshis = Satoshis(Int64(bytes.reverse))

  def write(satoshis: Satoshis): ByteVector =
    number.Int64(satoshis.toLong).bytes.reverse

}

object RawSatoshisSerializer extends RawSatoshisSerializer
