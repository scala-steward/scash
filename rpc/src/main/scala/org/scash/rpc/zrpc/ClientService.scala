package org.scash.rpc.zrpc

import java.util.UUID

import org.scash.rpc.BitcoindException
import play.api.libs.json._
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client._
import sttp.client.playJson._
import sttp.model.Uri
import zio.{ RIO, Task, URIO, ZIO }

import scala.util.{ Failure, Success, Try }

case class ClientService(uri: Uri, userName: String, password: String) {
  private val resultKey: String = "result"
  private val errorKey: String  = "error"

  def bitcoindCall[A](command: String, parameters: List[JsValue] = List.empty)(
    implicit reader: Reads[A]
  ): Task[A] =
    AsyncHttpClientZioBackend().toManaged(_.close().ignore).use { implicit backend =>
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
