package org.scash.testkit.gen

import org.scalacheck.Gen
import org.scash.core.protocol.{ Address, BitcoinAddress, P2PKHAddress, P2SHAddress }

/**
 * Created by chris on 6/12/17.
 */
sealed trait AddressGenerator {

  def p2pkhAddress: Gen[P2PKHAddress] =
    for {
      hash    <- CryptoGenerators.sha256Hash160Digest
      network <- ChainParamsGenerator.networkParams
      addr    = P2PKHAddress(hash, network)
    } yield addr

  def p2shAddress: Gen[P2SHAddress] =
    for {
      hash    <- CryptoGenerators.sha256Hash160Digest
      network <- ChainParamsGenerator.networkParams
      addr    = P2SHAddress(hash, network)
    } yield addr

  def bitcoinAddress: Gen[BitcoinAddress] = Gen.oneOf(p2pkhAddress, p2shAddress)

  def address: Gen[Address] = Gen.oneOf(p2pkhAddress, p2shAddress)
}

object AddressGenerator extends AddressGenerator
