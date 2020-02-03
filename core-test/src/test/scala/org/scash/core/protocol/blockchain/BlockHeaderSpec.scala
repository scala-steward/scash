package org.scash.core.protocol.blockchain

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.BlockchainElementsGenerator

/**
 * Created by tom on 7/6/16.
 */
class BlockHeaderSpec extends Properties("BlockHeaderSpec") {
  property("serialization symmetry") =
    Prop.forAll(BlockchainElementsGenerator.blockHeader) { header =>
      BlockHeader(header.hex) == header
    }
}
