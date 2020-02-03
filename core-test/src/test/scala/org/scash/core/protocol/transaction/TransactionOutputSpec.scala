package org.scash.core.protocol.transaction

import org.scalacheck.{Prop, Properties}
import org.scash.testkit.gen.TransactionGenerators

/**
 * Created by chris on 6/24/16.
 */
class TransactionOutputSpec extends Properties("TransactionOutputSpec") {

  property("Serialization symmetry") =
    Prop.forAll(TransactionGenerators.output) { output =>
      TransactionOutput(output.hex) == output
      output.hex == TransactionOutput(output.hex).hex
    }
}
