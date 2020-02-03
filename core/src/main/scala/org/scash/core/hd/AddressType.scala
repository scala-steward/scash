package org.scash.core.hd

/** The address types covered by BIP44, BIP49 and BIP84 */
sealed abstract class AddressType

object AddressType {
  final case object Legacy extends AddressType
}
