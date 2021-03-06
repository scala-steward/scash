package org.scash.core.serializers.p2p.messages

import org.scash.core.protocol.CompactSizeUInt
import org.scash.core.serializers.RawBitcoinSerializer
import org.scash.core.p2p._
import scodec.bits.ByteVector

/**
 * Responsible for serializing and deserializing a [[org.scash.core.p2p.FilterAddMessage FilterAddMessage]]
 * @see [[https://bitcoin.org/en/developer-reference#filteradd]]
 */
trait RawFilterAddMessageSerializer extends RawBitcoinSerializer[FilterAddMessage] {

  override def read(bytes: ByteVector): FilterAddMessage = {
    val elementSize = CompactSizeUInt.parseCompactSizeUInt(bytes)
    val element     = bytes.slice(elementSize.size.toInt, bytes.size)
    FilterAddMessage(elementSize, element)
  }

  override def write(filterAddMessage: FilterAddMessage): ByteVector =
    filterAddMessage.elementSize.bytes ++ filterAddMessage.element
}

object RawFilterAddMessageSerializer extends RawFilterAddMessageSerializer
