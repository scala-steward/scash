package org.scash.core.protocol.blockchain

import org.scash.core.crypto.DoubleSha256Digest
import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.MerkleGenerator

/**
 * Created by chris on 8/12/16.
 */
class MerkleBlockSpec extends Properties("MerkleBlockSpec") {

  //TODO: This is *extremely* slow, this is currently the longest running property we have taking about 6 minutes to run
  //I think it is the generator MerkleGenerator.merkleBlockWithInsertTxIds
  property("contains all inserted txids when we directly create a merkle block from the txids && " +
    "contains all txids matched by a bloom filter && " +
    "serialization symmetry") =
    Prop.forAllNoShrink(MerkleGenerator.merkleBlockWithInsertedTxIds) {
      case (merkleBlock: MerkleBlock, _, txIds: Seq[DoubleSha256Digest]) =>
        val extractedMatches = merkleBlock.partialMerkleTree.extractMatches
        extractedMatches == txIds &&
          extractedMatches.intersect(txIds) == txIds &&
          MerkleBlock(merkleBlock.hex) == merkleBlock
    }
}