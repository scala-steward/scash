package org.scash.core.serializers

import org.scash.core.currency.Satoshis
import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.CurrencyUnitGenerator

/**
 * Created by chris on 6/23/16.
 */
class RawSatoshisSerializerSpec extends Properties("RawSatoshiSerializerSpec") {

  property("Symmetrical serialization") =
    Prop.forAll(CurrencyUnitGenerator.satoshis) { satoshis =>
      Satoshis(satoshis.hex) == satoshis

    }

}
