package org.bitcoins.core.protocol

import org.bitcoins.core.config._
import org.bitcoins.core.crypto.{ ECPublicKey, HashDigest, Sha256Hash160Digest }
import org.bitcoins.core.protocol.transaction.TransactionOutput
import org.bitcoins.core.protocol.script._
import org.bitcoins.core.util._
import scodec.bits.ByteVector

import scala.util.{ Failure, Success, Try }

sealed abstract class Address {

  /** The network that this address is valid for */
  def networkParameters: NetworkParameters

  /** The string representation of this address */
  def value: String

  /** Every address is derived from a [[HashDigest]] in a [[TransactionOutput]] */
  def hash: HashDigest

  /** The [[ScriptPubKey]] the address represents */
  def scriptPubKey: ScriptPubKey

  override def toString = value
}

sealed abstract class BitcoinAddress extends Address

sealed abstract class P2PKHAddress extends BitcoinAddress {
  /** The base58 string representation of this address */
  override def value: String = {
    val versionByte = networkParameters.p2pkhNetworkByte
    val bytes = versionByte ++ hash.bytes
    val checksum = CryptoUtil.doubleSHA256(bytes).bytes.take(4)
    Base58.encode(bytes ++ checksum)
  }

  override def hash: Sha256Hash160Digest

  override def scriptPubKey: P2PKHScriptPubKey = P2PKHScriptPubKey(hash)

}

sealed abstract class P2SHAddress extends BitcoinAddress {
  /** The base58 string representation of this address */
  override def value: String = {
    val versionByte = networkParameters.p2shNetworkByte
    val bytes = versionByte ++ hash.bytes
    val checksum = CryptoUtil.doubleSHA256(bytes).bytes.take(4)
    Base58.encode(bytes ++ checksum)
  }

  override def scriptPubKey = P2SHScriptPubKey(hash)

  override def hash: Sha256Hash160Digest
}

/**
 * https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
 */

object P2PKHAddress extends AddressFactory[P2PKHAddress] {
  private case class P2PKHAddressImpl(
    hash: Sha256Hash160Digest,
    networkParameters: NetworkParameters) extends P2PKHAddress

  def apply(hash: Sha256Hash160Digest, network: NetworkParameters): P2PKHAddress = P2PKHAddressImpl(hash, network)

  def apply(pubKey: ECPublicKey, networkParameters: NetworkParameters): P2PKHAddress = {
    val hash = CryptoUtil.sha256Hash160(pubKey.bytes)
    P2PKHAddress(hash, networkParameters)
  }

  def apply(spk: P2PKHScriptPubKey, networkParameters: NetworkParameters): P2PKHAddress = {
    P2PKHAddress(spk.pubKeyHash, networkParameters)
  }

  override def fromString(address: String): Try[P2PKHAddress] = {
    val decodeCheckP2PKH: Try[ByteVector] = Base58.decodeCheck(address)
    decodeCheckP2PKH.flatMap { bytes =>
      val networkBytes: Option[(NetworkParameters, ByteVector)] = Networks.knownNetworks.map(n => (n, n.p2pkhNetworkByte))
        .find {
          case (_, bs) =>
            bytes.startsWith(bs)
        }
      val result: Option[P2PKHAddress] = networkBytes.map {
        case (network, p2pkhNetworkBytes) =>
          val payloadSize = bytes.size - p2pkhNetworkBytes.size
          require(payloadSize == 20, s"Payload of a P2PKH address must be 20 bytes in size, got $payloadSize")
          val payload = bytes.slice(p2pkhNetworkBytes.size, bytes.size)
          P2PKHAddress(Sha256Hash160Digest(payload), network)
      }
      result match {
        case Some(addr) => Success(addr)
        case None => Failure(new IllegalArgumentException(s"Given address was not a valid P2PKH address, got: $address"))
      }
    }
  }

  override def fromScriptPubKey(spk: ScriptPubKey, np: NetworkParameters): Try[P2PKHAddress] = spk match {
    case p2pkh: P2PKHScriptPubKey => Success(P2PKHAddress(p2pkh, np))
    case x @ (_: P2PKScriptPubKey | _: MultiSignatureScriptPubKey
      | _: P2SHScriptPubKey | _: LockTimeScriptPubKey
      | _: WitnessScriptPubKey | _: EscrowTimeoutScriptPubKey
      | _: NonStandardScriptPubKey | _: WitnessCommitment
      | _: UnassignedWitnessScriptPubKey | EmptyScriptPubKey) =>
      Failure(new IllegalArgumentException("Cannot create a address for the scriptPubKey: " + x))
  }
}

object P2SHAddress extends AddressFactory[P2SHAddress] {
  private case class P2SHAddressImpl(
    hash: Sha256Hash160Digest,
    networkParameters: NetworkParameters) extends P2SHAddress

  /**
   * Creates a [[P2SHScriptPubKey]] from the given [[ScriptPubKey]],
   * then creates an address from that [[P2SHScriptPubKey]]
   */
  def apply(scriptPubKey: ScriptPubKey, network: NetworkParameters): P2SHAddress = {
    val p2shScriptPubKey = P2SHScriptPubKey(scriptPubKey)
    P2SHAddress(p2shScriptPubKey, network)
  }

  def apply(p2shScriptPubKey: P2SHScriptPubKey, network: NetworkParameters): P2SHAddress = P2SHAddress(p2shScriptPubKey.scriptHash, network)

  def apply(hash: Sha256Hash160Digest, network: NetworkParameters): P2SHAddress = P2SHAddressImpl(hash, network)

  override def fromString(address: String): Try[P2SHAddress] = {
    val decodeCheckP2SH: Try[ByteVector] = Base58.decodeCheck(address)
    decodeCheckP2SH.flatMap { bytes =>
      val networkBytes: Option[(NetworkParameters, ByteVector)] = Networks.knownNetworks.map(n => (n, n.p2shNetworkByte))
        .find {
          case (_, bs) =>
            bytes.startsWith(bs)
        }
      val result: Option[P2SHAddress] = networkBytes.map {
        case (network, p2shNetworkBytes) =>
          val payloadSize = bytes.size - p2shNetworkBytes.size
          require(payloadSize == 20, s"Payload of a P2PKH address must be 20 bytes in size, got $payloadSize")
          val payload = bytes.slice(p2shNetworkBytes.size, bytes.size)
          P2SHAddress(Sha256Hash160Digest(payload), network)
      }
      result match {
        case Some(addr) => Success(addr)
        case None => Failure(new IllegalArgumentException(s"Given address was not a valid P2PKH address, got: $address"))
      }
    }
  }

  override def fromScriptPubKey(spk: ScriptPubKey, np: NetworkParameters): Try[P2SHAddress] = spk match {
    case p2sh: P2SHScriptPubKey => Success(P2SHAddress(p2sh, np))
    case x @ (_: P2PKScriptPubKey | _: P2PKHScriptPubKey | _: MultiSignatureScriptPubKey
      | _: LockTimeScriptPubKey | _: WitnessScriptPubKey
      | _: EscrowTimeoutScriptPubKey | _: NonStandardScriptPubKey
      | _: WitnessCommitment | _: UnassignedWitnessScriptPubKey | EmptyScriptPubKey) =>
      Failure(new IllegalArgumentException("Cannot create a address for the scriptPubKey: " + x))
  }
}

object BitcoinAddress extends AddressFactory[BitcoinAddress] {
  private val logger = BitcoinSLogger.logger

  /** Creates a [[BitcoinAddress]] from the given string value */
  def apply(value: String): Try[BitcoinAddress] = fromString(value)

  override def fromString(value: String): Try[BitcoinAddress] = {
    val p2pkhTry = P2PKHAddress.fromString(value)
    if (p2pkhTry.isSuccess) {
      p2pkhTry
    } else {
      val p2shTry = P2SHAddress.fromString(value)
      if (p2shTry.isSuccess) {
        p2shTry
      } else {
        Failure(new IllegalArgumentException(s"Could not decode the given value to a BitcoinAddress, got: $value"))
      }
    }
  }

  override def fromScriptPubKey(spk: ScriptPubKey, np: NetworkParameters): Try[BitcoinAddress] = spk match {
    case p2pkh: P2PKHScriptPubKey => Success(P2PKHAddress(p2pkh, np))
    case p2sh: P2SHScriptPubKey => Success(P2SHAddress(p2sh, np))
    case _ => Failure(new IllegalArgumentException("Cannot create an address for the scriptPubKey: " + spk))
  }

}

object Address extends AddressFactory[Address] {

  def fromBytes(bytes: ByteVector): Try[Address] = {
    val encoded = Base58.encode(bytes)
    BitcoinAddress.fromString(encoded)
  }

  def fromHex(hex: String): Try[Address] = fromBytes(BitcoinSUtil.decodeHex(hex))

  def apply(bytes: ByteVector): Try[Address] = fromBytes(bytes)

  def apply(str: String): Try[Address] = fromString(str)

  override def fromString(str: String): Try[Address] = {
    BitcoinAddress.fromString(str)
  }
  override def fromScriptPubKey(spk: ScriptPubKey, network: NetworkParameters): Try[Address] = network match {
    case bitcoinNetwork: BitcoinNetwork => BitcoinAddress.fromScriptPubKey(spk, network)
  }
  def apply(spk: ScriptPubKey, networkParameters: NetworkParameters): Try[Address] = {
    fromScriptPubKey(spk, networkParameters)
  }
}
