package org.scash.core.serializers.p2p.messages

import org.scash.core.currency.Satoshis
import org.scash.core.serializers.RawBitcoinSerializer
import org.scash.core.wallet.fee.SatoshisPerKiloByte
import org.scash.core.p2p._
import scodec.bits.ByteVector

sealed abstract class RawFeeFilterMessageSerializer
    extends RawBitcoinSerializer[FeeFilterMessage] {

  override def read(bytes: ByteVector): FeeFilterMessage = {
    val satBytes = bytes.take(8).reverse
    val sat = Satoshis(satBytes)
    val satPerKb = SatoshisPerKiloByte(sat)
    FeeFilterMessage(satPerKb)
  }

  override def write(feeFilterMessage: FeeFilterMessage): ByteVector = {
    feeFilterMessage.feeRate.currencyUnit.satoshis.bytes.reverse
  }
}

object RawFeeFilterMessageSerializer extends RawFeeFilterMessageSerializer
