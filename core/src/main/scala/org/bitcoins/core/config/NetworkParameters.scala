package org.bitcoins.core.config

import org.bitcoins.core.protocol.blockchain._
import scodec.bits.ByteVector

/**
 * * Copyright (c) 2015-2018, christewart (MIT License)
 * * Copyright (c) 2018-    , floreslorca (MIT License)
 */
sealed abstract class NetworkParameters {
  /** The parameters of the blockchain we are connecting to */
  def chainParams: ChainParams

  def p2pkhNetworkByte: ByteVector = chainParams.base58Prefix(Base58Type.PubKeyAddress)
  def p2shNetworkByte: ByteVector = chainParams.base58Prefix(Base58Type.ScriptAddress)
  def privateKey: ByteVector = chainParams.base58Prefix(Base58Type.SecretKey)

  def port: Int
  def rpcPort: Int
  def name: String = chainParams.networkId
  /** The seeds used to bootstrap the network */
  def dnsSeeds: Seq[String]

  /**
   * The message start string is designed to be unlikely to occur in normal data.
   * The characters are rarely used upper ASCII, not valid as UTF-8, and produce
   * a large 32-bit integer with any alignment.
   * https://github.com/bitcoin/bitcoin/blob/master/src/chainparams.cpp#L108
   */
  def magicBytes: ByteVector

}

sealed abstract class BitcoinNetwork extends NetworkParameters {
  override def chainParams: BitcoinChainParams
}

sealed abstract class MainNet extends BitcoinNetwork {
  override def chainParams = MainNetChainParams
  override def port = 8333
  override def rpcPort = 8332
  //mainnet doesn't need to be specified like testnet or regtest
  override def name = ""

  def dnsSeeds = List(
    "seed.bitcoinabc.org",
    "seed-abc.bitcoinforks.org",
    "seed.bitprim.org",
    "seed.deadalnix.me",
    "seeder.criptolayer.net")

  override def magicBytes = ByteVector(0xE3, 0xE1, 0xF3, 0xE8)

}

object MainNet extends MainNet

sealed abstract class TestNet3 extends BitcoinNetwork {
  override def chainParams = TestNetChainParams
  override def port = 18333
  override def rpcPort = 18332

  override def dnsSeeds = Seq(
    "testnet-seed.bitcoinabc.org",
    "testnet-seed-abc.bitcoinforks.org",
    "testnet-seed.bitprim.org",
    "testnet-seed.deadalnix.me",
    "testnet-seeder.criptolayer.net")

  override def magicBytes = ByteVector(0xF4, 0xE5, 0xF3, 0xF4)

}

object TestNet3 extends TestNet3

sealed abstract class RegTest extends BitcoinNetwork {
  override def chainParams = RegTestNetChainParams
  override def port = 18444
  override def rpcPort = TestNet3.rpcPort
  override def dnsSeeds = Nil
  override def magicBytes = ByteVector(0xDA, 0xB5, 0xBF, 0xFA)
}

object RegTest extends RegTest

object Networks {
  val knownNetworks: Seq[NetworkParameters] = Seq(MainNet, TestNet3, RegTest)
  val secretKeyBytes = knownNetworks.map(_.privateKey)
  val p2pkhNetworkBytes = knownNetworks.map(_.p2pkhNetworkByte)
  val p2shNetworkBytes = knownNetworks.map(_.p2shNetworkByte)

  def bytesToNetwork: Map[ByteVector, NetworkParameters] = Map(
    MainNet.p2shNetworkByte -> MainNet,
    MainNet.p2pkhNetworkByte -> MainNet,
    MainNet.privateKey -> MainNet,

    TestNet3.p2pkhNetworkByte -> TestNet3,
    TestNet3.p2shNetworkByte -> TestNet3,
    TestNet3.privateKey -> TestNet3

  //ommitting regtest as it has the same network bytes as testnet3
  )
}
