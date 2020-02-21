package org.scash.rpc.client.common

import org.scash.core.protocol.BitcoinAddress
import org.scash.core.protocol.script.ScriptPubKey
import org.scash.rpc.jsonmodels.{ DecodeScriptResult, ValidateAddressResult, ValidateAddressResultImpl }
import org.scash.rpc.serializers.JsonSerializers._
import org.scash.rpc.jsonmodels.{ DecodeScriptResult, ValidateAddressResult, ValidateAddressResultImpl }
import play.api.libs.json.{ JsString, Json }

import scala.concurrent.Future

/*
 * Utility RPC calls
 */
trait UtilRpc { self: Client =>

  def validateAddress(address: BitcoinAddress): Future[ValidateAddressResult] =
    bitcoindCall[ValidateAddressResultImpl]("validateaddress", List(JsString(address.toString)))

  def decodeScript(script: ScriptPubKey): Future[DecodeScriptResult] =
    bitcoindCall[DecodeScriptResult]("decodescript", List(Json.toJson(script)))
}
