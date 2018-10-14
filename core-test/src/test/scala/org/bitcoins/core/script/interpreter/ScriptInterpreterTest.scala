package org.bitcoins.core.script.interpreter

import org.bitcoins.core.crypto.BaseTxSigComponent
import org.bitcoins.core.currency.CurrencyUnits
import org.bitcoins.core.protocol.script._
import org.bitcoins.core.protocol.transaction.{ Transaction, TransactionOutput }
import org.bitcoins.core.script.{ PreExecutionScriptProgram, ScriptProgram }
import org.bitcoins.core.script.flag.ScriptFlagFactory
import org.bitcoins.core.script.interpreter.testprotocol.CoreTestCase
import org.bitcoins.core.script.interpreter.testprotocol.CoreTestCaseProtocol._
import org.bitcoins.core.util._
import org.scalatest.{ FlatSpec, MustMatchers }
import spray.json._

import scala.io.Source
/**
 * Created by chris on 1/6/16.
 */
class ScriptInterpreterTest extends FlatSpec with MustMatchers {
  private def logger = BitcoinSLogger.logger
  /*
  "ScriptInterpreter" must "evaluate all the scripts from the bitcoin core script_tests.json" in {

    val source = Source.fromURL(getClass.getResource("/script_tests.json"))

    //use this to represent a single test case from script_valid.json
    /*    val lines =
      """
          | [["", "DEPTH 0 EQUAL", "P2SH,STRICTENC", "OK", "Test the test: we should have an empty stack after scriptSig evaluation"]]
   """.stripMargin*/
    val lines = try source.getLines.filterNot(_.isEmpty).map(_.trim) mkString "\n" finally source.close()
    val json = lines.parseJson
    val testCasesOpt: Seq[Option[CoreTestCase]] = json.convertTo[Seq[Option[CoreTestCase]]]
    val testCases: Seq[CoreTestCase] = testCasesOpt.flatten
    for {
      testCase <- testCases
      (creditingTx, outputIndex) = TransactionTestUtil.buildCreditingTransaction(testCase.scriptPubKey)
      (tx, inputIndex) = TransactionTestUtil.buildSpendingTransaction(creditingTx, testCase.scriptSig, outputIndex)
    } yield {
      val scriptPubKey = ScriptPubKey.fromAsm(testCase.scriptPubKey.asm)
      val flags = ScriptFlagFactory.fromList(testCase.flags)
      val output = TransactionOutput(CurrencyUnits.zero, scriptPubKey)
      val txSigComponent = BaseTxSigComponent(
        transaction = tx,
        inputIndex = inputIndex,
        output = output,
        flags = flags)
      val program = PreExecutionScriptProgram(txSigComponent)
      withClue(testCase.raw) {
        ScriptInterpreter.run(program) must equal(testCase.expectedResult)
      }
    }
  }
  */
}
