package org.scash.rpc.zrpc

import play.api.libs.json.Reads
import play.api.libs.json.{ JsResult, JsValue }
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess

/**
 * Represents failures that can happen when using the
 * `bitcoind` RPC interface.
 *
 * @see [[https://github.com/bitcoin/bitcoin/blob/eb7daf4d600eeb631427c018a984a77a34aca66e/src/rpc/protocol.h#L32 protcol.h]]
 *      for an enumeration of all error codes used
 */
sealed trait BitcoindError {
  val message: String
  val code: Int
}

/**
 * Wallet errors from `bitcoind` RPC calls
 *
 * @see [[https://github.com/bitcoin/bitcoin/blob/eb7daf4d600eeb631427c018a984a77a34aca66e/src/rpc/protocol.h#L32 protcol.h]]
 *      for an enumeration of all error codes used
 */
object BitcoindError {
  implicit val reads: Reads[BitcoindError] = json =>
    for {
      code    <- (json \ "code").validate[Int]
      message <- (json \ "message").validate[String]
      error <- BitcoindError.fromCodeAndMessage(code, message) match {
                case None =>
                  JsError(s"Could not construct bitcoind exception with code $code and message '$message'")
                case Some(value) => JsSuccess(value)
              }
    } yield error

  private val all: List[String => BitcoindError] = List(
    InvalidParams(_),
    InternalError(_),
    ParseError(_),
    MiscError(_),
    TypeError(_),
    InvalidAddressOrKey(_),
    OutOfMemory(_),
    InvalidParameter(_),
    DatabaseError(_),
    DeserializationError(_),
    VerifyError(_),
    VerifyRejected(_),
    VerifyAlreadyInChain(_),
    InWarmUp(_),
    MethodDeprecated(_),
    ForbiddenBySafeMode(_),
    InInitialDownload(_),
    NodeAlreadyAdded(_),
    NodeNotAdded(_),
    NodeNotConnected(_),
    InvalidIpOrSubnet(_),
    P2PDisabled(_),
    WalletError(_),
    InsufficientFunds(_),
    InvalidLabelName(_),
    KeypoolRanOut(_),
    UnlockNeeded(_),
    PassphraseIncorrect(_),
    WrongEncState(_),
    EncryptionFailed(_),
    AlreadyUnlocked(_),
    NotFound(_),
    NotSpecified(_)
  )

  /** Attempts to construct a BitcoindError from the given code and message */
  def fromCodeAndMessage(code: Int, message: String): Option[BitcoindError] =
    all.find(f => f(message).code == code).map(f => f(message))

  final case class InvalidJSonParsing(message: String) extends BitcoindError {
    val code: Int = -1
  }

  final case class InvalidParams(message: String) extends BitcoindError {
    val code: Int = -32602
  }

  /**
   * InternalError is only used for genuine errors in bitcoind
   * (for example datadir corruption)
   */
  final case class InternalError(message: String) extends BitcoindError {
    val code: Int = -32603
  }
  final case class ParseError(message: String) extends BitcoindError {
    val code: Int = -32700
  }

  /** `std::exception` thrown in command handling*/
  final case class MiscError(message: String) extends BitcoindError {
    val code: Int = -1
  }

  /** Unexpected type was passed as parameter */
  final case class TypeError(message: String) extends BitcoindError {
    val code: Int = -3
  }

  /** Invalid address or key */
  final case class InvalidAddressOrKey(message: String) extends BitcoindError {
    val code: Int = -5
  }

  /**  Ran out of memory during operation*/
  final case class OutOfMemory(message: String) extends BitcoindError {
    val code: Int = -7
  }

  /** Invalid, missing or duplicate parameter */
  final case class InvalidParameter(message: String) extends BitcoindError {
    val code: Int = -8
  }

  /** Database error */
  final case class DatabaseError(message: String) extends BitcoindError {
    val code: Int = -20
  }

  /** Error parsing or validating structure in raw format */
  final case class DeserializationError(message: String) extends BitcoindError {
    val code: Int = -22
  }

  /** General error during transaction or block submission */
  final case class VerifyError(message: String) extends BitcoindError {
    val code: Int = -25
  }

  /** Transaction or block was rejected by network rules */
  final case class VerifyRejected(message: String) extends BitcoindError {
    val code: Int = -26
  }

  /** Transaction already in chain */
  final case class VerifyAlreadyInChain(message: String) extends BitcoindError {
    val code: Int = -27
  }

  /** Client still warming up */
  final case class InWarmUp(message: String) extends BitcoindError {
    val code: Int = -28
  }

  /** RPC method is deprecated */
  final case class MethodDeprecated(message: String) extends BitcoindError {
    val code: Int = -32
  }

  /**  Server is in safe mode, and command is not allowed in safe mode */
  final case class ForbiddenBySafeMode(message: String) extends BitcoindError {
    val code: Int = -2
  }

  /** Bitcoin is not connected */
  final case class NotConnected(message: String) extends BitcoindError {
    val code: Int = -9
  }

  /** Still downloading initial blocks */
  final case class InInitialDownload(message: String) extends BitcoindError {
    val code: Int = -10
  }

  /** Node is already added */
  final case class NodeAlreadyAdded(message: String) extends BitcoindError {
    val code: Int = -23
  }

  /** Node has not been added before */
  final case class NodeNotAdded(message: String) extends BitcoindError {
    val code: Int = -24
  }

  /** Node to disconnect not found in connected nodes */
  final case class NodeNotConnected(message: String) extends BitcoindError {
    val code: Int = -29
  }

  /** Invalid IP/Subnet */
  final case class InvalidIpOrSubnet(message: String) extends BitcoindError {
    val code: Int = -30
  }

  /** No valid connection manager instance found */
  final case class P2PDisabled(message: String) extends BitcoindError {
    val code: Int = -31
  }

  /** Unspecified problem with wallet (key not found etc.) */
  final case class WalletError(message: String) extends BitcoindError {
    val code: Int = -4
  }

  /** Not enough funds in wallet or account */
  final case class InsufficientFunds(message: String) extends BitcoindError {
    val code: Int = -6
  }

  /** Invalid label name */
  final case class InvalidLabelName(message: String) extends BitcoindError {
    val code: Int = -11
  }

  /** Keypool ran out, call keypoolrefill first */
  final case class KeypoolRanOut(message: String) extends BitcoindError {
    val code: Int = -12
  }

  /** Enter the wallet passphrase with walletpassphrase first */
  final case class UnlockNeeded(message: String) extends BitcoindError {
    val code: Int = -13
  }

  /** The wallet passphrase entered was incorrect */
  final case class PassphraseIncorrect(message: String) extends BitcoindError {
    val code: Int = -14
  }

  /** Command given in wrong wallet encryption state (encrypting an encrypted wallet etc.) */
  final case class WrongEncState(message: String) extends BitcoindError {
    val code: Int = -15
  }

  /** Failed to encrypt the wallet */
  final case class EncryptionFailed(message: String) extends BitcoindError {
    val code: Int = -16
  }

  /** Wallet is already unlocked */
  final case class AlreadyUnlocked(message: String) extends BitcoindError {
    val code: Int = -17
  }

  /**  Invalid wallet specified */
  final case class NotFound(message: String) extends BitcoindError {
    val code: Int = -18
  }

  /** No wallet specified (error when there are multiple wallets loaded) */
  final case class NotSpecified(message: String) extends BitcoindError {
    val code: Int = -19
  }

}
