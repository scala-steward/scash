package org.scash.rpc.zrpc

import play.api.libs.json.{ JsValue, Reads }
import sttp.model.Uri
import zio.{ RIO, Task }

trait ZClient {
  val rpc: ZClient.Service[Any]
}

object ZClient {
  trait Service[R] {
    def bitcoindCall[A](cmd: String, parameters: List[JsValue] = List.empty)(implicit r: Reads[A]): RIO[R, A]
  }

  final class Live(client: ClientService) extends Service[Any] {
    def bitcoindCall[A](cmd: String, parameters: List[JsValue] = List.empty)(implicit r: Reads[A]): Task[A] =
      client.bitcoindCall[A](cmd, parameters)
  }

  object Live {
    def make(uri: Uri, userName: String, password: String): Task[ZClient] =
      Task.succeed(
        new ZClient {
          val rpc: Service[Any] = new ZClient.Live(ClientService(uri, userName, password))
        }
      )
  }
}
