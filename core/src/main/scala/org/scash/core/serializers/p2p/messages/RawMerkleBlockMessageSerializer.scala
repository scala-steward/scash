package org.scash.core.serializers.p2p.messages

import org.scash.core.protocol.blockchain.MerkleBlock
import org.scash.core.serializers.RawBitcoinSerializer
import org.scash.core.util.BitcoinSLogger
import org.scash.core.p2p.MerkleBlockMessage
import scodec.bits.ByteVector

/**
  * Responsible for serialization and deserialization of MerkleBlockMessages
  * @see https://bitcoin.org/en/developer-reference#merkleblock
  */
trait RawMerkleBlockMessageSerializer
    extends RawBitcoinSerializer[MerkleBlockMessage]
    with BitcoinSLogger {

  def read(bytes: ByteVector): MerkleBlockMessage = {
    val merkleBlock = MerkleBlock(bytes)
    MerkleBlockMessage(merkleBlock)
  }

  def write(merkleBlockMessage: MerkleBlockMessage): ByteVector =
    merkleBlockMessage.merkleBlock.bytes

}

object RawMerkleBlockMessageSerializer extends RawMerkleBlockMessageSerializer
