package org.scash.core.crypto

import org.scash.core.script.crypto.SigHashType

import scodec.bits.ByteVector

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by chris on 7/21/16.
 */
sealed abstract class TransactionSignatureCreator {

  /**
   * Creates a signature from a tx signature component
   *
   * @param txSignatureComponent contains the tx, inputIndex which specify which input we are creating a sig for
   * @param privateKey the private key which we are signing the hash with
   * @param hashType the procedure to use for hashing to transaction
   * @return
   */
  def createSig(
    txSignatureComponent: TxSigComponent,
    privateKey: ECPrivateKey,
    hashType: SigHashType
  ): ECDigitalSignature = {
    val sign: ByteVector => ECDigitalSignature = privateKey.signECDSA(_: ByteVector)
    createSig(txSignatureComponent, sign, hashType)
  }

  /**
   * This is intended to be a low level hardware wallet API.
   * At a fundamental level, a hardware wallet expects a scodec.bits.ByteVector as input, and returns an [[ECDigitalSignature]]
   * if it is able to sign the scodec.bits.ByteVector's correctly.
   * @param component - the information needed to sign the transaction
   * @param sign - the implementation of the hardware wallet protocol to sign the scodec.bits.ByteVector w/ the given public key
   * @param hashType - the hash type to be appended on the digital signature when the hardware wallet is done being signed
   * @return the digital signature returned by the hardware wallet
   */
  def createSig(
    component: TxSigComponent,
    sign: ByteVector => ECDigitalSignature,
    hashType: SigHashType
  ): ECDigitalSignature = {
    val hash      = TransactionSignatureSerializer.hashForSignature(component, hashType)
    val signature = sign(hash.bytes)
    //append 1 byte hash type onto the end
    val sig = ECDigitalSignature(signature.bytes :+ hashType.byte)
    //require(HashType.hasForkId(sig), "The Signature doesnt have fork id")
    require(sig.isStrictEncoded, "We did not create a signature that is strictly encoded, got: " + sig)
    require(DERSignatureUtil.isLowS(sig), "Sig does not have a low s value")
    sig
  }

  /** This is the same as createSig above, except the 'sign' function returns a Future[ECDigitalSignature] */
  def createSig(component: TxSigComponent, sign: ByteVector => Future[ECDigitalSignature], hashType: SigHashType)(
    implicit ec: ExecutionContext
  ): Future[ECDigitalSignature] = {
    val hash      = TransactionSignatureSerializer.hashForSignature(component, hashType)
    val signature = sign(hash.bytes)
    //append 1 byte hash type onto the end
    val sig = signature.map(s => ECDigitalSignature(s.bytes :+ hashType.byte))
    sig.map { s =>
      require(s.isStrictEncoded, "We did not create a signature that is strictly encoded, got: " + sig)
      require(DERSignatureUtil.isLowS(s), "Sig does not have a low s value")
      s
    }
  }
}

object TransactionSignatureCreator extends TransactionSignatureCreator
