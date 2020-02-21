package org.scash.core.serializers.p2p.messages

import org.scash.core.number.UInt64
import org.scash.core.serializers.RawBitcoinSerializer
import org.scash.core.p2p.PongMessage
import org.scash.core.p2p.PongMessage
import scodec.bits.ByteVector

trait RawPongMessageSerializer extends RawBitcoinSerializer[PongMessage] {

  override def read(bytes: ByteVector): PongMessage =
    PongMessage(UInt64(bytes.take(8)))

  override def write(pongMessage: PongMessage): ByteVector =
    pongMessage.nonce.bytes
}

object RawPongMessageSerializer extends RawPongMessageSerializer
