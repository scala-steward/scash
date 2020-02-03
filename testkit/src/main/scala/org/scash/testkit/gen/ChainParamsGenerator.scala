package org.scash.testkit.gen

import org.scalacheck.Gen
import org.scash.core.config._

/**
 * Created by chris on 6/6/17.
 */
sealed abstract class ChainParamsGenerator {

  def networkParams: Gen[NetworkParameters] = bitcoinNetworkParams

  def bitcoinNetworkParams: Gen[BitcoinNetwork] = Gen.oneOf(MainNet, TestNet3, RegTest)
}

object ChainParamsGenerator extends ChainParamsGenerator
