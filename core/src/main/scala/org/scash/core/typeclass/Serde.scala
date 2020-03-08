package org.scash.core.typeclass

import scodec.bits.ByteVector
import scodec.{ Codec => SCodec }

object Serde {
  def apply[A](implicit c: SCodec[A]): SCodec[A] = c
}

trait SerdeSyntax {
  implicit class SerdeSyntaxOps[A: SCodec](a: A) {
    def bytesB: ByteVector = Serde[A].encode(a).require.toByteVector
    def hexB: String       = Serde[A].encode(a).require.toHex
  }
}
