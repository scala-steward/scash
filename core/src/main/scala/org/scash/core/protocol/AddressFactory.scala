package org.scash.core.protocol

import org.scash.core.protocol.script.ScriptPubKey
import org.scash.core.config.NetworkParameters

import scala.util.{ Failure, Success, Try }

abstract class AddressFactory[T] {

  /** Attempts to create an address from the given String */
  def fromString(str: String): Try[T]

  /** Same as fromString, but throws the exception */
  def fromStringExn(str: String): T = fromString(str) match {
    case Success(addr) => addr
    case Failure(exn) => throw exn
  }

  /**
   * Attempts to create a address from the given [[org.scash.core.protocol.script.ScriptPubKey]]
   * and [[NetworkParameters]]
   */
  def fromScriptPubKey(spk: ScriptPubKey, np: NetworkParameters): Try[T]

  /** Checks if the given string is a valid address */
  def isValid(str: String): Boolean = fromString(str).isSuccess
}