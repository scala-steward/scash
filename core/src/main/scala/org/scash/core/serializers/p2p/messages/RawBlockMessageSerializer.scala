package org.scash.core.serializers.p2p.messages

import org.scash.core.protocol.blockchain.Block
import org.scash.core.serializers.RawBitcoinSerializer
import org.scash.core.p2p.BlockMessage
import scodec.bits.ByteVector

trait RawBlockMessageSerializer extends RawBitcoinSerializer[BlockMessage] {

  def read(bytes: ByteVector): BlockMessage = {
    val block = Block.fromBytes(bytes)
    BlockMessage(block)
  }

  def write(blockMsg: BlockMessage): ByteVector = blockMsg.block.bytes
}

object RawBlockMessageSerializer extends RawBlockMessageSerializer
