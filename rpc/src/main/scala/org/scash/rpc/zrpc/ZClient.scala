package org.scash.rpc.zrpc

import java.nio.file.{ Path }
import java.util.UUID

import org.scash.core.config.{ MainNet, NetworkParameters, RegTest, TestNet3 }
import org.scash.core.crypto.ECPrivateKey

import org.scash.rpc.BitcoindException
import org.scash.rpc.client.common.RpcOpts

import org.scash.rpc.config.{ BitcoindInstance }
import org.scash.rpc.serializers.JsonSerializers._

import play.api.libs.json._
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client._
import sttp.client.playJson._
import zio.ZIO

import scala.util.{ Failure, Success, Try }

/**
 * This is the base trait for Bitcoin Core
 * RPC clients. It defines no RPC calls
 * except for the a ping. It contains functionality
 * and utilities useful when working with an RPC
 * client, like data directories, log files
 * and whether or not the client is started.
 */
final case class ZClient(instance: BitcoindInstance) {

  /**
   * The log file of the Bitcoin Core daemon
   */
  lazy val logFile: Path = {

    val prefix = instance.network match {
      case MainNet  => ""
      case TestNet3 => "testnet"
      case RegTest  => "regtest"
    }
    instance.datadir.toPath.resolve(prefix).resolve("debug.log")
  }

  /** The configuration file of the Bitcoin Core daemon */
  lazy val confFile: Path =
    instance.datadir.toPath.resolve("bitcoin.conf")

  implicit protected val network: NetworkParameters = instance.network

  /**
   * This is here (and not in JsonWriters)
   * so that the implicit network val is accessible
   */
  implicit object ECPrivateKeyWrites extends Writes[ECPrivateKey] {
    override def writes(o: ECPrivateKey): JsValue = JsString(o.toWIF(network))
  }

  implicit val eCPrivateKeyWrites: Writes[ECPrivateKey] = ECPrivateKeyWrites
  implicit val importMultiAddressWrites: Writes[RpcOpts.ImportMultiAddress] =
    Json.writes[RpcOpts.ImportMultiAddress]
  implicit val importMultiRequestWrites: Writes[RpcOpts.ImportMultiRequest] =
    Json.writes[RpcOpts.ImportMultiRequest]
  private val resultKey: String = "result"
  private val errorKey: String  = "error"

  def bitcoindCall[A](command: String, parameters: List[JsValue] = List.empty)(
    implicit reader: Reads[A]
  ): ZIO[ZClient, Throwable, A] =
    AsyncHttpClientZioBackend().flatMap { implicit backend =>
      val payload = JsObject(
        Map(
          "method" -> JsString(command),
          "params" -> JsArray(parameters),
          "id"     -> JsString(UUID.randomUUID().toString)
        )
      )

      val uri      = uri"http://${instance.rpcUri.getHost}:${instance.rpcUri.getPort}"
      val username = instance.authCredentials.username
      val password = instance.authCredentials.password

      val r = basicRequest
        .response(asJson[A])
        .post(uri)
        .body(payload)
        .auth
        .basic(username, password)
        .send()
      r.map(s => s.body).absolve
    }

  def parseJson[A](json: String)(implicit r: Reads[A]): Either[Throwable, A] =
    Try(Json.parse(json)) match {
      case Failure(e) => Left(e)
      case Success(js) => {
        val result = (js \ "resultKey").validate[A]
        checkUnitError(result, js).getOrElse(
          (js \ "resultKey").validate[A] match {
            case JsSuccess(t, _) => Right(t)
            case _: JsError      => parseError(js)
          }
        )
      }
    }
  def response[A](implicit r: Reads[A]) =
    asStringAlways.map(j => parseJson[A](j))

  // Catches errors thrown by calls with Unit as the expected return type (which isn't handled by UnitReads)
  private def checkUnitError[A](
    result: JsResult[A],
    json: JsValue
  ): Option[Either[BitcoindException, Nothing]] =
    if (result == JsSuccess(())) {
      (json \ errorKey).validate[BitcoindException] match {
        case JsSuccess(err, _) => Some(Left(err))
        case _: JsError        => None
      }
    } else None

  def parseError(js: JsValue) =
    (js \ "errorKey").validate[BitcoindException] match {
      case JsSuccess(err, _) => Left(err)
      case res: JsError =>
        val jsonResult = (js \ resultKey).get
        val errString =
          s"Error when parsing: ${JsError.toJson(res).toString}!"
        Left(new IllegalArgumentException(s"Could not parse JsResult: $jsonResult! Error: $errString"))
    }
}
