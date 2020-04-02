package org.scash.core.typeclass

import scodec.bits.ByteVector
import scodec.Codec

object Serde {
  def apply[A](implicit c: Codec[A]): Codec[A] = c

  def fromBytes[A: Codec](byteVector: ByteVector): A = implicitly[Codec[A]].decode(byteVector.bits).require.value
}

trait SerdeSyntax {
  implicit class SerdeSyntaxOps[A: Codec](a: A) {
    def bytesB: ByteVector = Serde[A].encode(a).require.toByteVector
    def hexB: String       = Serde[A].encode(a).require.toHex
  }
  implicit class ByteVectorOps(byteVector: ByteVector) {
    def decode[A: Codec]: A = Serde.fromBytes(byteVector)
  }
}
