package org.scash.core.serializers.p2p.messages

import org.scash.core.serializers.RawBitcoinSerializer
import org.scash.core.serializers.bloom.RawBloomFilterSerializer
import org.scash.core.p2p.FilterLoadMessage
import scodec.bits.ByteVector

/**
  * Serializes and deserializes a [[org.scash.core.p2p.FilterLoadMessage FilterLoadMessage]]
  * @see [[https://bitcoin.org/en/developer-reference#filterload]]
  */
trait RawFilterLoadMessageSerializer
    extends RawBitcoinSerializer[FilterLoadMessage] {

  override def read(bytes: ByteVector): FilterLoadMessage = {
    val filter = RawBloomFilterSerializer.read(bytes)
    FilterLoadMessage(filter)
  }

  override def write(filterLoadMessage: FilterLoadMessage): ByteVector = {
    RawBloomFilterSerializer.write(filterLoadMessage.bloomFilter)
  }
}

object RawFilterLoadMessageSerializer extends RawFilterLoadMessageSerializer
