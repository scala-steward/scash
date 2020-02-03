package org.scash.core.hd

/** This is a field that is used in conjunction with BIP44 to indicate
  * what the purpose of this [[org.scash.core.crypto.ExtKey ExtKey]] is.
  *
  *
  * Format:
  * m / purpose'
  *
  * @see [[https://github.com/bitcoin/bips/blob/master/bip-0043.mediawiki BIP43]]
  * @see [[https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki#Purpose BIP44]]
  * @see [[https://github.com/bitcoin/bips/blob/master/bip-0084.mediawiki BIP84]]
  * @see [[https://github.com/bitcoin/bips/blob/master/bip-0049.mediawiki BIP49]]
  * */
case class HDPurpose(constant: Int) extends BIP32Path {
  override val path: Vector[BIP32Node] = Vector(
    BIP32Node(constant, hardened = true))
}

object HDPurposes {
  final val Legacy = HDPurpose(LegacyHDPath.PURPOSE)

  lazy val all: Vector[HDPurpose] = Vector(Legacy)

  /** Tries to turn the provided integer into a HD purpose path segment */
  def fromConstant(i: Int): Option[HDPurpose] = all.find(_.constant == i)
}
