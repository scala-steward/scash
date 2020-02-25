package org.scash.rpc.zrpc

import java.util.UUID

import org.scash.rpc.BitcoindException

import org.scash.rpc.serializers.JsonSerializers._
import play.api.libs.json._

import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client._
import sttp.client.playJson._

import zio.{ RIO, ZIO }

import scala.util.{ Failure, Success, Try }

object ClientService {
  private val resultKey: String = "result"
  private val errorKey: String  = "error"

  def bitcoindCall[A](command: String, parameters: List[JsValue] = List.empty)(
    implicit reader: Reads[A]
  ): RIO[ZConfig, A] =
    AsyncHttpClientZioBackend().flatMap { implicit backend =>
      val payload = JsObject(
        Map(
          "method" -> JsString(command),
          "params" -> JsArray(parameters),
          "id"     -> JsString(UUID.randomUUID().toString)
        )
      )

      val response = for {
        env <- ZIO.environment[ZConfig]
        r <- basicRequest
              .response(asStringAlways.map(parseJson[A]))
              .post(env.uri)
              .body(payload)
              .auth
              .basic(env.userName, env.passWord)
              .send()
      } yield r.body
      response.absolve
    }

  def parseJson[A](json: String)(implicit r: Reads[A]): Either[Throwable, A] =
    Try(Json.parse(json)) match {
      case Failure(e) => Left(e)
      case Success(js) => {
        val result = (js \ resultKey).validate[A]
        checkUnitError(result, js).getOrElse(
          result match {
            case JsSuccess(t, _) => Right(t)
            case _: JsError      => parseError(js)
          }
        )
      }
    }

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
        Left(
          new IllegalArgumentException(s"Could not parse JsResult: ${Json.prettyPrint(jsonResult)}! Error: $errString")
        )
    }
}
