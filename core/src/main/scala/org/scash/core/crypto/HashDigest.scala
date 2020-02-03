package org.scash.core.crypto

import org.scash.core.protocol.NetworkElement
import org.scash.core.util.Factory
import org.slf4j.{Logger, LoggerFactory}
import scodec.bits.ByteVector
/**
 * Created by chris on 5/24/16.
 */
sealed abstract class HashDigest extends NetworkElement {
  /** The message digest represented in bytes */
  def bytes: ByteVector
  /**
   * Flips the endianness of the byte sequence.
   * Since bitcoin unfortunately has inconsistent endianness between the protocol
   * level and the presentation level. This is useful for switching between the two.
   * @return
   */
  def flip: HashDigest
}

/**
 * Represents the result of SHA1()
 */
sealed abstract class Sha1Digest extends HashDigest {
  override def flip: Sha1Digest = Sha1Digest(bytes.reverse)
}

object Sha1Digest extends Factory[Sha1Digest] {
  private case class Sha1DigestImpl(bytes: ByteVector) extends Sha1Digest {
    override def toString = s"Sha1DigestImpl($hex)"
  }
  override def fromBytes(bytes: ByteVector): Sha1Digest = Sha1DigestImpl(bytes)
}

/**
 * Represents the result of SHA256()
 */
sealed abstract class Sha256Digest extends HashDigest {
  override def flip: Sha256Digest = Sha256Digest(bytes.reverse)
}

object Sha256Digest extends Factory[Sha256Digest] {
  private case class Sha256DigestImpl(bytes: ByteVector) extends Sha256Digest {
    require(bytes.length == 32, "Sha256Digest must be 32 bytes in size, got: " + bytes.length)
    override def toString = s"Sha256DigestImpl($hex)"
  }
  override def fromBytes(bytes: ByteVector): Sha256Digest = Sha256DigestImpl(bytes)
}

/**
 * Represents the result of SHA256(SHA256())
 */
sealed abstract class DoubleSha256Digest extends HashDigest {
  def flip: DoubleSha256DigestBE = DoubleSha256DigestBE(bytes.reverse)
}

object DoubleSha256Digest extends Factory[DoubleSha256Digest] {
  private case class DoubleSha256DigestImpl(bytes: ByteVector) extends DoubleSha256Digest {
    require(bytes.length == 32, "DoubleSha256Digest must always be 32 bytes, got: " + bytes.length)
    override def toString = s"DoubleSha256DigestImpl($hex)"
  }
  override def fromBytes(bytes: ByteVector): DoubleSha256Digest = DoubleSha256DigestImpl(bytes)

  val empty: DoubleSha256Digest = DoubleSha256Digest(
    ByteVector.low(32)
    )
}

case class DoubleSha256DigestBE(bytes: ByteVector) extends HashDigest {
  require(bytes.length == 32,
          "DoubleSha256Digest must always be 32 bytes, got: " + bytes.length)

  def flip: DoubleSha256Digest =
    DoubleSha256Digest.fromBytes(bytes.reverse)

  override def toString = s"DoubleSha256BDigestBE($hex)"
}

object DoubleSha256DigestBE extends Factory[DoubleSha256DigestBE] {
  override def fromBytes(bytes: ByteVector): DoubleSha256DigestBE =
  // have to use new to avoid infinite loop
    new DoubleSha256DigestBE(bytes)

  val empty: DoubleSha256DigestBE = DoubleSha256DigestBE(ByteVector.low(32))
}

/**
 * Represents the result of RIPEMD160()
 */
sealed abstract class RipeMd160Digest extends HashDigest {
  override def flip: RipeMd160DigestBE = RipeMd160DigestBE(bytes.reverse)
}

object RipeMd160Digest extends Factory[RipeMd160Digest] {
  private case class RipeMd160DigestImpl(bytes: ByteVector) extends RipeMd160Digest {
    require(bytes.length == 20, "RIPEMD160Digest must always be 20 bytes, got: " + bytes.length)
    override def toString = s"RipeMd160DigestImpl($hex)"
  }
  override def fromBytes(bytes: ByteVector): RipeMd160Digest = RipeMd160DigestImpl(bytes)
}

sealed trait RipeMd160DigestBE extends HashDigest {
  override def flip: RipeMd160Digest = RipeMd160Digest(bytes.reverse)
}

object RipeMd160DigestBE extends Factory[RipeMd160DigestBE] {
  private case class RipeMd160DigestBEImpl(bytes: ByteVector)
    extends RipeMd160DigestBE {
    override def toString = s"RipeMd160DigestBEImpl($hex)"
    // $COVERAGE-ON$
  }
  override def fromBytes(bytes: ByteVector): RipeMd160DigestBE = {
    require(bytes.length == 20,
            // $COVERAGE-OFF$
            "RIPEMD160Digest must always be 20 bytes, got: " + bytes.length)
    RipeMd160DigestBEImpl(bytes)
  }
}

/**
 * Represents the result of RIPEMD160(SHA256())
 */
sealed abstract class Sha256Hash160Digest extends HashDigest {
  override def flip: Sha256Hash160Digest = Sha256Hash160Digest(bytes.reverse)
}

object Sha256Hash160Digest extends Factory[Sha256Hash160Digest] {
  private case class Sha256Hash160DigestImpl(bytes: ByteVector) extends Sha256Hash160Digest {
    require(bytes.length == 20, "Sha256Hash160Digest must always be 20 bytes, got: " + bytes.length)
    override def toString = s"Sha256Hash160DigestImpl($hex)"
  }
  override def fromBytes(bytes: ByteVector): Sha256Hash160Digest = Sha256Hash160DigestImpl(bytes)
}
