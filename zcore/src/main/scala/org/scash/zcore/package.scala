package org.scash

import org.scash.zcore.typeclass.{ CNumeric, NumericSyntax, SerdeSyntax }

package object zcore extends SerdeSyntax with NumericSyntax {

  implicit val bigIntNumeric: CNumeric[BigInt] =
    CNumeric[BigInt](0xFFFFFFFFFFFFFFFFL)(identity, identity)

  implicit val longNumeric: CNumeric[Long] =
    CNumeric[Long](0xFFFFFFFFFFFFFFFFL)(identity[Long], _.toLong)

  implicit val intNumeric: CNumeric[Int] =
    CNumeric[Int](0xFFFFFFFF)(identity[Int], _.toInt)

}
