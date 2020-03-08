package org.scash.core.typeclass

import scala.util.{ Failure, Success, Try }

trait Numeric[A] { self =>
  def andMask: BigInt
  def num: A => BigInt
  def apply: BigInt => A

  def sum[A1: Numeric](a: A, a1: A1): A = checkResult(num(a) + Numeric[A1].num(a1))
  def sub[A1: Numeric](a: A, a1: A1): A = checkResult(num(a) - Numeric[A1].num(a1))
  def mul[A1: Numeric](a: A, a1: A1): A = checkResult(num(a) * Numeric[A1].num(a1))

  def gt[A1: Numeric](a: A, a1: A1): Boolean  = num(a) > Numeric[A1].num(a1)
  def gte[A1: Numeric](a: A, a1: A1): Boolean = num(a) >= Numeric[A1].num(a1)
  def lt[A1: Numeric](a: A, a1: A1): Boolean  = num(a) < Numeric[A1].num(a1)
  def lte[A1: Numeric](a: A, a1: A1): Boolean = num(a) <= Numeric[A1].num(a1)

  def shiftL(a: A, a1: A): A =
    checkIfInt(a1).map(_ => apply((num(a) << num(a1).toInt) & andMask)).get

  def shiftR(a: A, a1: A): A =
    //this check is for weird behavior with the jvm and shift rights
    //https://stackoverflow.com/questions/47519140/bitwise-shift-right-with-long-not-equaling-zero/47519728#47519728
    if (num(a1).toLong > 63) apply(0)
    else {
      checkIfInt(a1).map(_ => apply(num(a) >> num(a1).toInt)).get
    }

  def or[A1: Numeric](a: A, a1: A1): A  = checkResult(num(a) | Numeric[A1].num(a1))
  def and[A1: Numeric](a: A, a1: A1): A = checkResult(num(a) & Numeric[A1].num(a1))
  def xor[A1: Numeric](a: A, a1: A1): A = checkResult(num(a) ^ Numeric[A1].num(a1))

  def negative(a: A): A = apply(-num(a))

  def toLong(a: A): Long = num(a).bigInteger.longValueExact()
  def toInt(a: A): Int   = num(a).bigInteger.intValueExact()

  private def checkResult(result: BigInt): A = {
    require((result & andMask) == result, "Result was out of bounds, got: " + result)
    apply(result)
  }

  /** Checks if the given number is within range of a Int */
  private def checkIfInt(n: A): Try[Unit] =
    if (num(n) >= Int.MaxValue || num(n) <= Int.MinValue) {
      Failure(new IllegalArgumentException("Num was not in range of int, got: " + num))
    } else {
      Success(())
    }
}

object Numeric {
  def apply[A](implicit n: Numeric[A]): Numeric[A] = n

  def apply[A](n: A => BigInt, app: BigInt => A, mask: BigInt): Numeric[A] = new Numeric[A] {
    def andMask = mask
    def num     = n
    def apply   = app
  }
}

trait NumericSyntax {
  implicit class NumericSyntaxOps[A: Numeric](a: A) {
    def +[A1: Numeric](num: A1): A        = Numeric[A].sum(a, num)
    def -[A1: Numeric](num: A1): A        = Numeric[A].sub(a, num)
    def *[A1: Numeric](num: A1): A        = Numeric[A].mul(a, num)
    def >[A1: Numeric](num: A1): Boolean  = Numeric[A].gt(a, num)
    def >=[A1: Numeric](num: A1): Boolean = Numeric[A].gte(a, num)
    def <[A1: Numeric](num: A1): Boolean  = Numeric[A].lt(a, num)
    def <=[A1: Numeric](num: A1): Boolean = Numeric[A].lte(a, num)

    def <<(num: Int): A = Numeric[A].shiftL(a, Numeric[A].apply(BigInt(num)))
    def >>(num: Int): A = Numeric[A].shiftR(a, Numeric[A].apply(BigInt(num)))

    def <<(num: A): A = Numeric[A].shiftL(a, num)
    def >>(num: A): A = Numeric[A].shiftR(a, num)

    def |(num: A): A = Numeric[A].or(a, num)
    def &(num: A): A = Numeric[A].and(a, num)
    def ^(num: A): A = Numeric[A].xor(a, num)
    def unary_- : A  = Numeric[A].negative(a)
  }

}
