package org.scash.core.crypto

import scodec.bits.ByteVector
import scala.concurrent.Future

/**
 * This is meant to be an abstraction for a [[ECPrivateKey]], sometimes we will not
 * have direct access to a private key in memory -- for instance if that key is on a hardware device -- so we need to create an
 * abstraction of the signing process. Fundamentally a private key takes in a scodec.bits.ByteVector and returns a [[ECDigitalSignature]]
 * That is what this abstraction is meant to represent. If you have a [[ECPrivateKey]] in your application, you can get it's
 * [[Sign]] type by doing this:
 *
 * val key = ECPrivateKey()
 * val sign: scodec.bits.ByteVector => Future[ECDigitalSignature] = key.signFunction
 *
 * If you have a hardware wallet, you will need to implement the protocol to send a message to the hardware device. The
 * type signature of the function you implement must be scodec.bits.ByteVector => Future[ECDigitalSignature]
 *
 */
trait Sign {
  def signECDSAFunction: ByteVector => Future[ECDigitalSignature]

  def signECDSA(bytes: ByteVector): ECDigitalSignature

  def publicKey: ECPublicKey
}

