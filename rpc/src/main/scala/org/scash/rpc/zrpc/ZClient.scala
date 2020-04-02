package org.scash.rpc.zrpc

import java.util.UUID

import org.scash.rpc.zrpc.BitcoindError.InvalidJSonParsing
import org.scash.rpc.zrpc.zrpc.ZClient
import play.api.libs.json._
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client.playJson._
import sttp.client.{ asStringAlways, basicRequest }
import sttp.model.Uri
import zio.{ IO, Managed, ZLayer }

import scala.util.{ Failure, Success, Try }

object ZClient {
  trait Service {
    def bitcoindCall[A: Reads](cmd: String, parameters: List[JsValue] = List.empty): IO[BitcoindError, A]
  }

  def make(
    uri: Uri,
    userName: String,
    password: String
  ): ZLayer[Any, Throwable, ZClient] =
    ZLayer.fromManaged(
      Managed.make(AsyncHttpClientZioBackend())(_.close().ignore).map { implicit sttp =>
        new ZClient.Service {
          def bitcoindCall[A: Reads](command: String, parameters: List[JsValue] = List.empty): IO[BitcoindError, A] = {
            val payload = JsObject(
              Map(
                "method" -> JsString(command),
                "params" -> JsArray(parameters),
                "id"     -> JsString(UUID.randomUUID().toString)
              )
            )

            val response = for {
              r <- basicRequest
                    .response(asStringAlways.map(parseJson[A]))
                    .post(uri)
                    .body(payload)
                    .auth
                    .basic(userName, password)
                    .send()
            } yield r.body

            response.orDie.absolve
          }
        }
      }
    )

  private val resultKey: String = "result"
  private val errorKey: String  = "error"

  private def parseJson[A: Reads](json: String): Either[BitcoindError, A] =
    Try(Json.parse(json)) match {
      case Failure(e) => Left(InvalidJSonParsing(e.getMessage))
      case Success(js) =>
        val result = (js \ resultKey).validate[A]
        result match {
          case JsSuccess((), _) => parseError[A](js).getOrElse(Right(result.get))
          case JsSuccess(t, _)  => Right(t)
          case res: JsError =>
            parseError(js).getOrElse {
              val errString = s"Error when parsing: ${JsError.toJson(res).toString}!"
              Left(
                InvalidJSonParsing(
                  s"Client Error: Could not cast JsResult: ${Json.prettyPrint(js)}! Error: $errString"
                )
              )
            }
        }
    }

  private def parseError[A](js: JsValue) =
    (js \ errorKey).validate[BitcoindError] match {
      case JsSuccess(err, _) => Some(Left(err))
      case _: JsError        => None
    }
}
