package org.scash.core.serializers.p2p.messages

import org.scash.core.number.UInt64
import org.scash.core.serializers.RawBitcoinSerializer
import org.scash.core.p2p.ServiceIdentifier
import scodec.bits.ByteVector

/**
  * Responsible for serializing and deserializing the
  * service identifier in a network message
  * @see https://bitcoin.org/en/developer-reference#version
  */
trait RawServiceIdentifierSerializer
    extends RawBitcoinSerializer[ServiceIdentifier] {

  override def read(bytes: ByteVector): ServiceIdentifier = {
    val serviceBytes = bytes.take(8)
    //since bitcoin uses big endian for numbers, we need to convert to little endian
    ServiceIdentifier(UInt64(serviceBytes.reverse))
  }

  override def write(serviceIdentifier: ServiceIdentifier): ByteVector = {
    serviceIdentifier.num.bytes.reverse
  }

}

object RawServiceIdentifierSerializer extends RawServiceIdentifierSerializer
