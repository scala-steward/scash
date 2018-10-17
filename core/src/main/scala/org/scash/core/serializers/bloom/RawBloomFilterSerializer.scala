package org.scash.core.serializers.bloom

import org.scash.core.bloom.{BloomFilter, BloomFlag}
import org.scash.core.number.UInt32
import org.scash.core.protocol.CompactSizeUInt
import org.scash.core.serializers.RawBitcoinSerializer
import scodec.bits.ByteVector

/**
 * Created by chris on 8/4/16.
 * [[https://github.com/bitcoin/bips/blob/master/bip-0037.mediawiki#new-messages]]
 */
sealed abstract class RawBloomFilterSerializer extends RawBitcoinSerializer[BloomFilter] {

  override def read(bytes: ByteVector): BloomFilter = {
    val filterSize = CompactSizeUInt.parseCompactSizeUInt(bytes)
    val filter = bytes.slice(filterSize.size.toInt, filterSize.size.toInt + filterSize.num.toInt)
    val hashFuncsIndex = (filterSize.size + filterSize.num.toInt).toInt
    val hashFuncs = UInt32(bytes.slice(hashFuncsIndex, hashFuncsIndex + 4).reverse)
    val tweakIndex = hashFuncsIndex + 4
    val tweak = UInt32(bytes.slice(tweakIndex, tweakIndex + 4).reverse)
    val flags = BloomFlag(bytes(tweakIndex + 4))
    BloomFilter(filterSize, filter, hashFuncs, tweak, flags)

  }

  override def write(bloomFilter: BloomFilter): ByteVector = {
    bloomFilter.filterSize.bytes ++ bloomFilter.data ++
      bloomFilter.hashFuncs.bytes.reverse ++ bloomFilter.tweak.bytes.reverse ++
      ByteVector.fromByte(bloomFilter.flags.byte)
  }
}

object RawBloomFilterSerializer extends RawBloomFilterSerializer
