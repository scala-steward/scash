package org.scash.core.crypto

import org.scash.core.config.{MainNet, RegTest, TestNet3}
import org.scash.core.gen.CryptoGenerators
import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.{ChainParamsGenerator, CryptoGenerators}

/**
 * Created by chris on 7/25/16.
 */
class ECPrivateKeySpec extends Properties("ECPrivateKeySpec") {

  property("Serialization symmetry for WIF format") =
    Prop.forAll(CryptoGenerators.privateKey, ChainParamsGenerator.networkParams) { (privKey, network) =>
      val wif = privKey.toWIF(network)
      val isCorrectNetwork = network match {
        case MainNet => ECPrivateKey.parseNetworkFromWIF(wif).get == network
        case TestNet3 | RegTest => ECPrivateKey.parseNetworkFromWIF(wif).get == TestNet3
      }
      ECPrivateKey.fromWIFToPrivateKey(wif) == privKey && isCorrectNetwork
    }

  property("Serialization symmetry") =
    Prop.forAll(CryptoGenerators.privateKey) { privKey =>
      ECPrivateKey(privKey.hex) == privKey
    }

  property("unique key generation") =
    Prop.forAll(CryptoGenerators.privateKey, CryptoGenerators.privateKey) { (privKey1, privKey2) =>
      privKey1 != privKey2
    }
}
