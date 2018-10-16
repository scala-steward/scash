package org.scash.core.protocol.blockchain

import org.scash.core.protocol.transaction.Transaction
import org.scash.core.protocol.{ CompactSizeUInt, NetworkElement }
import org.scash.core.serializers.blockchain.RawBlockSerializer
import org.scash.core.number.UInt64
import org.scash.core.util.Factory
import scodec.bits.ByteVector

/**
 * Created by chris on 5/19/16.
 * Represents a block in our blockchain
 * Bitcoin Core implementation:
 * [[https://github.com/bitcoin/bitcoin/blob/master/src/primitives/block.h#L73]]
 * Bitcoin Developer Reference link:
 * [[https://bitcoin.org/en/developer-reference#serialized-blocks]]
 */
sealed abstract class Block extends NetworkElement {

  /** The block header for this block */
  def blockHeader: BlockHeader

  /**
   * The total number of transactions in this block,
   * including the coinbase transaction.
   */
  def txCount: CompactSizeUInt

  /** The transactions contained in this block */
  def transactions: Seq[Transaction]

  override def bytes = RawBlockSerializer.write(this)

}

/**
 * Companion object for creating Blocks
 */
object Block extends Factory[Block] {

  private sealed case class BlockImpl(
    blockHeader: BlockHeader,
    txCount: CompactSizeUInt, transactions: Seq[Transaction]) extends Block

  def apply(blockHeader: BlockHeader, txCount: CompactSizeUInt, transactions: Seq[Transaction]): Block = {
    BlockImpl(blockHeader, txCount, transactions)
  }

  def apply(blockHeader: BlockHeader, transactions: Seq[Transaction]): Block = {
    val txCount = CompactSizeUInt(UInt64(transactions.size))
    Block(blockHeader, txCount, transactions)
  }

  def fromBytes(bytes: ByteVector): Block = RawBlockSerializer.read(bytes)

}
