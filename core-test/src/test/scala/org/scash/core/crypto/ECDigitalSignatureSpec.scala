package org.scash.core.crypto

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.CryptoGenerators

/**
 * Created by chris on 8/16/16.
 */
class ECDigitalSignatureSpec extends Properties("ECDigitalSignatureSpec") {

  property("must be der encoded") =
    Prop.forAll(CryptoGenerators.digitalSignature) { signature =>
      signature.isDEREncoded
    }

  property("must have a low s") =
    Prop.forAll(CryptoGenerators.digitalSignature) { signature =>
      DERSignatureUtil.isLowS(signature)
    }

  property("must create and verify a digital signature") =
    Prop.forAll(CryptoGenerators.doubleSha256Digest, CryptoGenerators.privateKey) {
      case (hash, key) =>
        val sig = key.signECDSA(hash)
        key.publicKey.verifyECDSA(hash, sig)
    }

  property("must not reuse r values") = {
    Prop.forAll(CryptoGenerators.privateKey, CryptoGenerators.doubleSha256Digest, CryptoGenerators.doubleSha256Digest) {
      case (key, hash1, hash2) =>
        val sig1 = key.signECDSA(hash1)
        val sig2 = key.signECDSA(hash2)
        sig1.r != sig2.r
    }
  }
  property("must have serialization symmetry with r,s") = {
    Prop.forAll(CryptoGenerators.digitalSignature) {
      case sig: ECDigitalSignature =>
        ECDigitalSignature.fromRS(sig.r, sig.s) == sig
    }
  }
}
