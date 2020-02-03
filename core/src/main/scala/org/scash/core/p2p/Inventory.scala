package org.scash.core.p2p

import org.scash.core.crypto.DoubleSha256Digest
import org.scash.core.protocol.NetworkElement
import org.scash.core.util.Factory
import org.scash.core.serializers.p2p.messages.RawInventorySerializer
import scodec.bits.ByteVector

/**
  * These are used as unique identifiers inside the peer-to-peer network
  *
  * @param typeIdentifier The type of object which was hashed
  * @param hash SHA256(SHA256()) hash of the object in internal byte order.
  *
  * @see [[https://bitcoin.org/en/developer-reference#term-inventory]]
  */
case class Inventory(typeIdentifier: TypeIdentifier, hash: DoubleSha256Digest)
    extends NetworkElement {

  override def bytes: ByteVector = RawInventorySerializer.write(this)
}

object Inventory extends Factory[Inventory] {

  override def fromBytes(bytes: ByteVector): Inventory =
    RawInventorySerializer.read(bytes)

}
