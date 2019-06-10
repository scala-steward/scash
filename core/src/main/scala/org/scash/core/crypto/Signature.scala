package org.scash.core.crypto

import scodec.bits.ByteVector

sealed trait Signature

case class SchnorrSignature(bytes: ByteVector) extends Signature
case class ECDSignature(bytes: ByteVector) extends Signature
