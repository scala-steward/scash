package org.scash.rpc.zio

import org.scash.core.config.MainNet
import org.scash.core.crypto.DoubleSha256DigestBE
import org.scash.rpc.jsonmodels.{ Bip9Softfork, GetBlockChainInfoResult, Softfork }
import zio._
import zio.test._
import zio.test.Assertion._
import org.scash.rpc.serializers.JsonSerializers._
import play.api.libs.json._

object SerializerSpec
    extends DefaultRunnableSpec(
      suite("BitcoinRPC")(
        testM("GetBlockchainInfo") {
          val p = """{
            "chain": "main",
            "blocks": 568749,
            "headers": 568749,
            "bestblockhash": "0000000000000000056c84123ed3469668c95d302bb1f3a2c3da9471ed7ba95c",
            "difficulty": 188284050802.8465,
            "mediantime": 1549568282,
            "verificationprogress": 0.7179360160311195,
            "initialblockdownload": true,
            "chainwork": "000000000000000000000000000000000000000000ddd938dee0aae5f0722d82",
            "size_on_disk": 159089098034,
            "pruned": false,
            "softforks": {
              "minerfund": {
                "type": "bip9",
                "bip9": {
                  "status": "defined",
                  "start_time": 1573819200,
                  "timeout": 1589544000,
                  "since": 0
                },
                "active": false
              },
              "minerfundabc": {
                "type": "bip9",
                "bip9": {
                  "status": "defined",
                  "start_time": 1573819200,
                  "timeout": 1589544000,
                  "since": 0
                },
                "active": false
              }
            },
            "warnings": ""
            }"""
          val ans = GetBlockChainInfoResult(
            MainNet,
            568749,
            568749,
            DoubleSha256DigestBE("0000000000000000056c84123ed3469668c95d302bb1f3a2c3da9471ed7ba95c"),
            BigDecimal(188284050802.8465),
            1549568282,
            BigDecimal(0.7179360160311195),
            true,
            "000000000000000000000000000000000000000000ddd938dee0aae5f0722d82",
            159089098034L,
            false,
            None,
            Map(
              "minerfund"    -> Softfork("bip9", Bip9Softfork("defined", 1573819200, 1589544000, 0), false),
              "minerfundabc" -> Softfork("bip9", Bip9Softfork("defined", 1573819200, 1589544000, 0), false)
            ),
            ""
          )
          assertM(ZIO.succeed(Json.parse(p).validate[GetBlockChainInfoResult].get), equalTo(ans))
        },
        testM("Softfork") {

          val p = Json.parse(
            """{
              "type": "bip9",
              "bip9": {
                "status": "defined",
                "start_time": 1573819200,
                "timeout": 1589544000,
                "since": 0
              },
              "active": false}"""
          )

          val r = Softfork("bip9", Bip9Softfork("defined", 1573819200, 1589544000, 0), false)
          assertM(ZIO.succeed(p.validate[Softfork].get), equalTo(r))
        },
        testM("Bip9Softfork") {

          val e = Json.parse(
            """{ 
               "status" : "defined", 
               "start_time" : 1573819200, 
               "timeout" : 1589544000, 
               "since" : 0 
               }"""
          )

          val j = Json.fromJson[Bip9Softfork](e)
          val r = Bip9Softfork("defined", 1573819200, 1589544000, 0)

          ZIO.succeed(assert(j.get, equalTo(r)))
        }
      )
    )
