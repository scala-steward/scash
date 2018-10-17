package org.scash.core.script.interpreter

import org.scash.core.consensus.Consensus
import org.scash.core.currency.{CurrencyUnit, CurrencyUnits}
import org.scash.core.protocol.CompactSizeUInt
import org.scash.core.protocol.script._
import org.scash.core.protocol.transaction._
import org.scash.core.script
import org.scash.core.script._
import org.scash.core.script.arithmetic._
import org.scash.core.script.bitwise._
import org.scash.core.script.constant._
import org.scash.core.script.control._
import org.scash.core.script.crypto._
import org.scash.core.script.flag._
import org.scash.core.script.locktime._
import org.scash.core.script.reserved._
import org.scash.core.script.result._
import org.scash.core.script.splice._
import org.scash.core.script.stack._
import org.scash.core.util.{BitcoinSLogger, BitcoinSUtil, BitcoinScriptUtil}

import scala.annotation.tailrec
/**
 * Created by chris on 1/6/16.
 */
sealed abstract class ScriptInterpreter {

  private def logger = BitcoinSLogger.logger
  /**
   * Currently bitcoin core limits the maximum number of non-push operations per script
   * to 201
   */
  private lazy val maxScriptOps = 201

  /** We cannot push an element larger than 520 bytes onto the stack */
  private lazy val maxPushSize = 520

  /**
   * Runs an entire script though our script programming language and
   * returns a [[ScriptResult]] indicating if the script was valid, or if not what error it encountered
   */
  def run(program: PreExecutionScriptProgram): ScriptResult = {
    val scriptSig = program.txSignatureComponent.scriptSignature
    val scriptPubKey = program.txSignatureComponent.scriptPubKey
    val flags = program.flags
    val p2shEnabled = ScriptFlagUtil.p2shEnabled(flags)
    val executedProgram: ExecutedScriptProgram = if (ScriptFlagUtil.requirePushOnly(flags)
      && !BitcoinScriptUtil.isPushOnly(program.script)) {
      logger.error("We can only have push operations inside of the script sig when the SIGPUSHONLY flag is set")
      ScriptProgram(program, ScriptErrorSigPushOnly)
    } else if (scriptSig.isInstanceOf[P2SHScriptSignature] && p2shEnabled &&
      !BitcoinScriptUtil.isPushOnly(scriptSig.asm)) {
      logger.error("P2SH scriptSigs are required to be push only by definition - see BIP16, got: " + scriptSig.asm)
      ScriptProgram(program, ScriptErrorSigPushOnly)
    } else {
      val scriptSigExecutedProgram = loop(program, 0)
      logger.trace(s"scriptSigExecutedProgram $scriptSigExecutedProgram")
      val t = scriptSigExecutedProgram.txSignatureComponent
      val scriptPubKeyProgram = ExecutionInProgressScriptProgram(t, scriptSigExecutedProgram.stack, t.scriptPubKey.asm.toList,
        t.scriptPubKey.asm.toList, Nil, scriptSigExecutedProgram.flags, None)
      val scriptPubKeyExecutedProgram: ExecutedScriptProgram = loop(scriptPubKeyProgram, 0)
      logger.trace(s"scriptPubKeyExecutedProgram $scriptPubKeyExecutedProgram")
      if (scriptSigExecutedProgram.error.isDefined) {
        scriptSigExecutedProgram
      } else if (scriptPubKeyExecutedProgram.error.isDefined || scriptPubKeyExecutedProgram.stackTopIsFalse) {
        scriptPubKeyExecutedProgram
      } else {
        scriptPubKey match {
          case p2sh: P2SHScriptPubKey =>
            if (p2shEnabled) executeP2shScript(scriptSigExecutedProgram, program, p2sh)
            else scriptPubKeyExecutedProgram
          case _: P2PKHScriptPubKey | _: P2PKScriptPubKey | _: MultiSignatureScriptPubKey | _: CSVScriptPubKey
            | _: CLTVScriptPubKey | _: NonStandardScriptPubKey | _: EscrowTimeoutScriptPubKey | EmptyScriptPubKey =>
            scriptPubKeyExecutedProgram
        }
      }
    }
    logger.trace("Executed Script Program: " + executedProgram)
    if (executedProgram.error.isDefined) executedProgram.error.get
    else if (executedProgram.stackTopIsTrue && flags.contains(ScriptVerifyCleanStack)) {
      //require that the stack after execution has exactly one element on it
      if (executedProgram.stack.size == 1) ScriptOk
      else ScriptErrorCleanStack
    } else if (executedProgram.stackTopIsTrue) ScriptOk
    else ScriptErrorEvalFalse
  }

  /**
   * Runs the given [[PreExecutionScriptProgram]] and
   * return if that script was valid or not
   */
  def runVerify(p: PreExecutionScriptProgram): Boolean = { ScriptInterpreter.run(p) == ScriptOk }

  /**
   * Every given [[script.PreExecutionScriptProgram]] and returns
   * it's [[org.scash.core.script.result.ScriptResult]]
   */
  def runAll(programs: Seq[PreExecutionScriptProgram]): Seq[ScriptResult] = {
    programs.map(p => ScriptInterpreter.run(p))
  }

  /**
   * Runs all the given [[ScriptProgram]] and return
   * if it is valid or not
   */
  def runAllVerify(programs: Seq[PreExecutionScriptProgram]): Boolean = {
    !programs.exists(p => ScriptInterpreter.run(p) != ScriptOk)
  }
  /**
   * P2SH scripts are unique in their evaluation, first the scriptSignature must be added to the stack, next the
   * p2sh scriptPubKey must be run to make sure the serialized redeem script hashes to the value found in the p2sh
   * scriptPubKey, then finally the serialized redeemScript is decoded and run with the arguments in the p2sh script signature
   * a p2sh script returns true if both of those intermediate steps evaluate to true
   *
   * @param scriptPubKeyExecutedProgram the program with the script signature pushed onto the stack
   * @param originalProgram the original program, used for setting errors & checking that the original script signature contains push only tokens
   * @param p2shScriptPubKey the p2sh scriptPubKey that contains the value the redeemScript must hash to
   * @return the executed program
   */
  private def executeP2shScript(scriptPubKeyExecutedProgram: ExecutedScriptProgram, originalProgram: ScriptProgram, p2shScriptPubKey: P2SHScriptPubKey): ExecutedScriptProgram = {

    /** Helper function to actually run a p2sh script */
    def run(p: ExecutedScriptProgram, stack: Seq[ScriptToken], s: ScriptPubKey): ExecutedScriptProgram = {
      logger.debug("Running p2sh script: " + stack)
      val p2shRedeemScriptProgram = ExecutionInProgressScriptProgram(
        txSignatureComponent = p.txSignatureComponent,
        stack = p.stack.tail,
        script = s.asm.toList,
        originalScript = p.originalScript,
        altStack = Nil,
        flags = p.flags,
        lastCodeSeparator = None)

      /*ScriptProgram(p.txSignatureComponent, stack.tail,s.asm)*/
      if (ScriptFlagUtil.requirePushOnly(p2shRedeemScriptProgram.flags) && !BitcoinScriptUtil.isPushOnly(s.asm)) {
        logger.error("p2sh redeem script must be push only operations whe SIGPUSHONLY flag is set")
        ScriptProgram(p2shRedeemScriptProgram, ScriptErrorSigPushOnly)
      } else loop(p2shRedeemScriptProgram, 0)
    }

    val scriptSig = scriptPubKeyExecutedProgram.txSignatureComponent.scriptSignature
    val scriptSigAsm: Seq[ScriptToken] = scriptSig.asm
    //need to check if the scriptSig is push only as required by bitcoin core
    //https://github.com/bitcoin/bitcoin/blob/528472111b4965b1a99c4bcf08ac5ec93d87f10f/src/script/interpreter.cpp#L1419
    if (!BitcoinScriptUtil.isPushOnly(scriptSigAsm)) {
      ScriptProgram(scriptPubKeyExecutedProgram, ScriptErrorSigPushOnly)
    } else if (scriptPubKeyExecutedProgram.error.isDefined) {
      scriptPubKeyExecutedProgram
    } else if (scriptPubKeyExecutedProgram.stackTopIsTrue) {
      logger.debug("Hashes matched between the p2shScriptSignature & the p2shScriptPubKey")
      //we need to run the deserialized redeemScript & the scriptSignature without the serialized redeemScript
      val stack = scriptPubKeyExecutedProgram.stack
      val redeemScriptBytes = stack.head.bytes
      val c = CompactSizeUInt.calculateCompactSizeUInt(redeemScriptBytes)
      val redeemScript = ScriptPubKey(c.bytes ++ redeemScriptBytes)
      logger.debug("redeemScript: " + redeemScript.asm)
      run(scriptPubKeyExecutedProgram, stack, redeemScript)
    } else {
      logger.warn("P2SH scriptPubKey hash did not match the hash for the serialized redeemScript")
      scriptPubKeyExecutedProgram
    }
  }

  /**
   * The execution loop for a script
   *
   * @param program the program whose script needs to be evaluated
   * @return program the final state of the program after being evaluated by the interpreter
   */
  @tailrec
  private def loop(program: ScriptProgram, opCount: Int): ExecutedScriptProgram = {
    logger.trace("Stack: " + program.stack)
    logger.trace("Script: " + program.script)
    val scriptByteVector = BitcoinSUtil.toByteVector(program.script)
    if (opCount > maxScriptOps && !program.isInstanceOf[ExecutedScriptProgram]) {
      logger.error("We have reached the maximum amount of script operations allowed")
      logger.error("Here are the remaining operations in the script: " + program.script)
      loop(ScriptProgram(program, ScriptErrorOpCount), opCount)
    } else if (scriptByteVector.length > 10000 && !program.isInstanceOf[ExecutedScriptProgram]) {
      logger.error("We cannot run a script that is larger than 10,000 bytes")
      program match {
        case p: PreExecutionScriptProgram =>
          loop(ScriptProgram(ScriptProgram.toExecutionInProgress(p), ScriptErrorScriptSize), opCount)
        case _: ExecutionInProgressScriptProgram | _: ExecutedScriptProgram =>
          loop(ScriptProgram(program, ScriptErrorScriptSize), opCount)
      }
    } else {
      program match {
        case p: PreExecutionScriptProgram => loop(ScriptProgram.toExecutionInProgress(p, Some(p.stack)), opCount)
        case p: ExecutedScriptProgram =>
          val countedOps = program.originalScript.map(BitcoinScriptUtil.countsTowardsScriptOpLimit(_)).count(_ == true)
          logger.trace("Counted ops: " + countedOps)
          if (countedOps > maxScriptOps && p.error.isEmpty) {
            loop(ScriptProgram(p, ScriptErrorOpCount), opCount)
          } else p

        case p: ExecutionInProgressScriptProgram =>
          p.script match {
            //if at any time we see that the program is not valid
            //cease script execution
            case _ if p.script.intersect(Seq(OP_VERIF, OP_VERNOTIF)).nonEmpty =>
              logger.error("Script is invalid even when a OP_VERIF or OP_VERNOTIF occurs in an unexecuted OP_IF branch")
              loop(ScriptProgram(p, ScriptErrorBadOpCode), opCount)
            //disabled splice operation
            case _ if p.script.intersect(Seq(OP_CAT, OP_SUBSTR, OP_LEFT, OP_RIGHT)).nonEmpty =>
              logger.error("Script is invalid because it contains a disabled splice operation")
              loop(ScriptProgram(p, ScriptErrorDisabledOpCode), opCount)
            //disabled bitwise operations
            case _ if p.script.intersect(Seq(OP_INVERT, OP_AND, OP_OR, OP_XOR)).nonEmpty =>
              logger.error("Script is invalid because it contains a disabled bitwise operation")
              loop(ScriptProgram(p, ScriptErrorDisabledOpCode), opCount)
            //disabled arithmetic operations
            case _ if p.script.intersect(Seq(OP_MUL, OP_2MUL, OP_DIV, OP_2DIV, OP_MOD, OP_LSHIFT, OP_RSHIFT)).nonEmpty =>
              logger.error("Script is invalid because it contains a disabled arithmetic operation")
              loop(ScriptProgram(p, ScriptErrorDisabledOpCode), opCount)
            //program cannot contain a push operation > 520 bytes
            case _ if (p.script.exists(token => token.bytes.size > maxPushSize)) =>
              logger.error("We have a script constant that is larger than 520 bytes, this is illegal: " + p.script)
              loop(ScriptProgram(p, ScriptErrorPushSize), opCount)
            //program stack size cannot be greater than 1000 elements
            case _ if ((p.stack.size + p.altStack.size) > 1000) =>
              logger.error("We cannot have a stack + alt stack size larger than 1000 elements")
              loop(ScriptProgram(p, ScriptErrorStackSize), opCount)

            //stack operations
            case OP_DUP :: t => loop(StackInterpreter.opDup(p), calcOpCount(opCount, OP_DUP))
            case OP_DEPTH :: t => loop(StackInterpreter.opDepth(p), calcOpCount(opCount, OP_DEPTH))
            case OP_TOALTSTACK :: t => loop(StackInterpreter.opToAltStack(p), calcOpCount(opCount, OP_TOALTSTACK))
            case OP_FROMALTSTACK :: t => loop(StackInterpreter.opFromAltStack(p), calcOpCount(opCount, OP_FROMALTSTACK))
            case OP_DROP :: t => loop(StackInterpreter.opDrop(p), calcOpCount(opCount, OP_DROP))
            case OP_IFDUP :: t => loop(StackInterpreter.opIfDup(p), calcOpCount(opCount, OP_IFDUP))
            case OP_NIP :: t => loop(StackInterpreter.opNip(p), calcOpCount(opCount, OP_NIP))
            case OP_OVER :: t => loop(StackInterpreter.opOver(p), calcOpCount(opCount, OP_OVER))
            case OP_PICK :: t => loop(StackInterpreter.opPick(p), calcOpCount(opCount, OP_PICK))
            case OP_ROLL :: t => loop(StackInterpreter.opRoll(p), calcOpCount(opCount, OP_ROLL))
            case OP_ROT :: t => loop(StackInterpreter.opRot(p), calcOpCount(opCount, OP_ROT))
            case OP_2ROT :: t => loop(StackInterpreter.op2Rot(p), calcOpCount(opCount, OP_2ROT))
            case OP_2DROP :: t => loop(StackInterpreter.op2Drop(p), calcOpCount(opCount, OP_2DROP))
            case OP_SWAP :: t => loop(StackInterpreter.opSwap(p), calcOpCount(opCount, OP_SWAP))
            case OP_TUCK :: t => loop(StackInterpreter.opTuck(p), calcOpCount(opCount, OP_TUCK))
            case OP_2DUP :: t => loop(StackInterpreter.op2Dup(p), calcOpCount(opCount, OP_2DUP))
            case OP_3DUP :: t => loop(StackInterpreter.op3Dup(p), calcOpCount(opCount, OP_3DUP))
            case OP_2OVER :: t => loop(StackInterpreter.op2Over(p), calcOpCount(opCount, OP_2OVER))
            case OP_2SWAP :: t => loop(StackInterpreter.op2Swap(p), calcOpCount(opCount, OP_2SWAP))

            //arithmetic operations
            case OP_ADD :: t => loop(ArithmeticInterpreter.opAdd(p), calcOpCount(opCount, OP_ADD))
            case OP_1ADD :: t => loop(ArithmeticInterpreter.op1Add(p), calcOpCount(opCount, OP_1ADD))
            case OP_1SUB :: t => loop(ArithmeticInterpreter.op1Sub(p), calcOpCount(opCount, OP_1SUB))
            case OP_SUB :: t => loop(ArithmeticInterpreter.opSub(p), calcOpCount(opCount, OP_SUB))
            case OP_ABS :: t => loop(ArithmeticInterpreter.opAbs(p), calcOpCount(opCount, OP_ABS))
            case OP_NEGATE :: t => loop(ArithmeticInterpreter.opNegate(p), calcOpCount(opCount, OP_NEGATE))
            case OP_NOT :: t => loop(ArithmeticInterpreter.opNot(p), calcOpCount(opCount, OP_NOT))
            case OP_0NOTEQUAL :: t => loop(ArithmeticInterpreter.op0NotEqual(p), calcOpCount(opCount, OP_0NOTEQUAL))
            case OP_BOOLAND :: t => loop(ArithmeticInterpreter.opBoolAnd(p), calcOpCount(opCount, OP_BOOLAND))
            case OP_BOOLOR :: t => loop(ArithmeticInterpreter.opBoolOr(p), calcOpCount(opCount, OP_BOOLOR))
            case OP_NUMEQUAL :: t => loop(ArithmeticInterpreter.opNumEqual(p), calcOpCount(opCount, OP_NUMEQUAL))
            case OP_NUMEQUALVERIFY :: t => loop(ArithmeticInterpreter.opNumEqualVerify(p), calcOpCount(opCount, OP_NUMEQUALVERIFY))
            case OP_NUMNOTEQUAL :: t => loop(ArithmeticInterpreter.opNumNotEqual(p), calcOpCount(opCount, OP_NUMNOTEQUAL))
            case OP_LESSTHAN :: t => loop(ArithmeticInterpreter.opLessThan(p), calcOpCount(opCount, OP_LESSTHAN))
            case OP_GREATERTHAN :: t => loop(ArithmeticInterpreter.opGreaterThan(p), calcOpCount(opCount, OP_GREATERTHAN))
            case OP_LESSTHANOREQUAL :: t => loop(ArithmeticInterpreter.opLessThanOrEqual(p), calcOpCount(opCount, OP_LESSTHANOREQUAL))
            case OP_GREATERTHANOREQUAL :: t => loop(ArithmeticInterpreter.opGreaterThanOrEqual(p), calcOpCount(opCount, OP_GREATERTHANOREQUAL))
            case OP_MIN :: t => loop(ArithmeticInterpreter.opMin(p), calcOpCount(opCount, OP_MIN))
            case OP_MAX :: t => loop(ArithmeticInterpreter.opMax(p), calcOpCount(opCount, OP_MAX))
            case OP_WITHIN :: t => loop(ArithmeticInterpreter.opWithin(p), calcOpCount(opCount, OP_WITHIN))

            //bitwise operations
            case OP_EQUAL :: t => loop(BitwiseInterpreter.opEqual(p), calcOpCount(opCount, OP_EQUAL))

            case OP_EQUALVERIFY :: t => loop(BitwiseInterpreter.opEqualVerify(p), calcOpCount(opCount, OP_EQUALVERIFY))

            case OP_0 :: t => loop(ScriptProgram(p, ScriptNumber.zero :: p.stack, t), calcOpCount(opCount, OP_0))
            case (scriptNumberOp: ScriptNumberOperation) :: t =>
              loop(ScriptProgram(p, ScriptNumber(scriptNumberOp.toLong) :: p.stack, t), calcOpCount(opCount, scriptNumberOp))
            case (bytesToPushOntoStack: BytesToPushOntoStack) :: t =>
              loop(ConstantInterpreter.pushScriptNumberBytesToStack(p), calcOpCount(opCount, bytesToPushOntoStack))
            case (scriptNumber: ScriptNumber) :: t =>
              loop(ScriptProgram(p, scriptNumber :: p.stack, t), calcOpCount(opCount, scriptNumber))
            case OP_PUSHDATA1 :: t => loop(ConstantInterpreter.opPushData1(p), calcOpCount(opCount, OP_PUSHDATA1))
            case OP_PUSHDATA2 :: t => loop(ConstantInterpreter.opPushData2(p), calcOpCount(opCount, OP_PUSHDATA2))
            case OP_PUSHDATA4 :: t => loop(ConstantInterpreter.opPushData4(p), calcOpCount(opCount, OP_PUSHDATA4))

            case (x: ScriptConstant) :: t => loop(ScriptProgram(p, x :: p.stack, t), calcOpCount(opCount, x))

            //control operations
            case OP_IF :: t => loop(ControlOperationsInterpreter.opIf(p), calcOpCount(opCount, OP_IF))
            case OP_NOTIF :: t => loop(ControlOperationsInterpreter.opNotIf(p), calcOpCount(opCount, OP_NOTIF))
            case OP_ELSE :: t => loop(ControlOperationsInterpreter.opElse(p), calcOpCount(opCount, OP_ELSE))
            case OP_ENDIF :: t => loop(ControlOperationsInterpreter.opEndIf(p), calcOpCount(opCount, OP_ENDIF))
            case OP_RETURN :: t => loop(ControlOperationsInterpreter.opReturn(p), calcOpCount(opCount, OP_RETURN))

            case OP_VERIFY :: t => loop(ControlOperationsInterpreter.opVerify(p), calcOpCount(opCount, OP_VERIFY))

            //crypto operations
            case OP_HASH160 :: t => loop(CryptoInterpreter.opHash160(p), calcOpCount(opCount, OP_HASH160))
            case OP_CHECKSIG :: t => loop(CryptoInterpreter.opCheckSig(p), calcOpCount(opCount, OP_CHECKSIG))
            case OP_CHECKSIGVERIFY :: t => loop(CryptoInterpreter.opCheckSigVerify(p), calcOpCount(opCount, OP_CHECKSIGVERIFY))
            case OP_SHA1 :: t => loop(CryptoInterpreter.opSha1(p), calcOpCount(opCount, OP_SHA1))
            case OP_RIPEMD160 :: t => loop(CryptoInterpreter.opRipeMd160(p), calcOpCount(opCount, OP_RIPEMD160))
            case OP_SHA256 :: t => loop(CryptoInterpreter.opSha256(p), calcOpCount(opCount, OP_SHA256))
            case OP_HASH256 :: t => loop(CryptoInterpreter.opHash256(p), calcOpCount(opCount, OP_HASH256))
            case OP_CODESEPARATOR :: t => loop(CryptoInterpreter.opCodeSeparator(p), calcOpCount(opCount, OP_CODESEPARATOR))
            case OP_CHECKMULTISIG :: t =>
              CryptoInterpreter.opCheckMultiSig(p) match {
                case newProgram: ExecutedScriptProgram =>
                  //script was marked invalid for other reasons, don't need to update the opcount
                  loop(newProgram, opCount)
                case newProgram @ (_: ExecutionInProgressScriptProgram | _: PreExecutionScriptProgram) =>
                  val newOpCount = calcOpCount(opCount, OP_CHECKMULTISIG) + BitcoinScriptUtil.numPossibleSignaturesOnStack(program).toInt
                  loop(newProgram, newOpCount)
              }
            case OP_CHECKMULTISIGVERIFY :: t =>
              CryptoInterpreter.opCheckMultiSigVerify(p) match {
                case newProgram: ExecutedScriptProgram =>
                  //script was marked invalid for other reasons, don't need to update the opcount
                  loop(newProgram, opCount)
                case newProgram @ (_: ExecutionInProgressScriptProgram | _: PreExecutionScriptProgram) =>
                  val newOpCount = calcOpCount(opCount, OP_CHECKMULTISIGVERIFY) + BitcoinScriptUtil.numPossibleSignaturesOnStack(program).toInt
                  loop(newProgram, newOpCount)
              }
            //reserved operations
            case OP_NOP :: t =>
              //script discourage upgradeable flag does not apply to a OP_NOP
              loop(ScriptProgram(p, p.stack, t), calcOpCount(opCount, OP_NOP))

            //if we see an OP_NOP and the DISCOURAGE_UPGRADABLE_OP_NOPS flag is set we must fail our program
            case (nop: NOP) :: t if ScriptFlagUtil.discourageUpgradableNOPs(p.flags) =>
              logger.error("We cannot execute a NOP when the ScriptVerifyDiscourageUpgradableNOPs is set")
              loop(ScriptProgram(p, ScriptErrorDiscourageUpgradableNOPs), calcOpCount(opCount, nop))
            case (nop: NOP) :: t => loop(ScriptProgram(p, p.stack, t), calcOpCount(opCount, nop))
            case OP_RESERVED :: t =>
              logger.error("OP_RESERVED automatically marks transaction invalid")
              loop(ScriptProgram(p, ScriptErrorBadOpCode), calcOpCount(opCount, OP_RESERVED))
            case OP_VER :: t =>
              logger.error("Transaction is invalid when executing OP_VER")
              loop(ScriptProgram(p, ScriptErrorBadOpCode), calcOpCount(opCount, OP_VER))
            case OP_RESERVED1 :: t =>
              logger.error("Transaction is invalid when executing OP_RESERVED1")
              loop(ScriptProgram(p, ScriptErrorBadOpCode), calcOpCount(opCount, OP_RESERVED1))
            case OP_RESERVED2 :: t =>
              logger.error("Transaction is invalid when executing OP_RESERVED2")
              loop(ScriptProgram(p, ScriptErrorBadOpCode), calcOpCount(opCount, OP_RESERVED2))

            case (reservedOperation: ReservedOperation) :: t =>
              logger.error("Undefined operation found which automatically fails the script: " + reservedOperation)
              loop(ScriptProgram(p, ScriptErrorBadOpCode), calcOpCount(opCount, reservedOperation))
            //splice operations
            case OP_SIZE :: t => loop(SpliceInterpreter.opSize(p), calcOpCount(opCount, OP_SIZE))

            //locktime operations
            case OP_CHECKLOCKTIMEVERIFY :: t =>
              //check if CLTV is enforced yet
              if (ScriptFlagUtil.checkLockTimeVerifyEnabled(p.flags)) {
                loop(LockTimeInterpreter.opCheckLockTimeVerify(p), calcOpCount(opCount, OP_CHECKLOCKTIMEVERIFY))
              } //if not, check to see if we should discourage p
              else if (ScriptFlagUtil.discourageUpgradableNOPs(p.flags)) {
                logger.error("We cannot execute a NOP when the ScriptVerifyDiscourageUpgradableNOPs is set")
                loop(ScriptProgram(p, ScriptErrorDiscourageUpgradableNOPs), calcOpCount(opCount, OP_CHECKLOCKTIMEVERIFY))
              } //in this case, just reat OP_CLTV just like a NOP and remove it from the stack
              else loop(ScriptProgram(p, p.script.tail, ScriptProgram.Script), calcOpCount(opCount, OP_CHECKLOCKTIMEVERIFY))
            case OP_CHECKSEQUENCEVERIFY :: t =>
              //check if CLTV is enforced yet
              if (ScriptFlagUtil.checkSequenceVerifyEnabled(p.flags)) {
                loop(LockTimeInterpreter.opCheckSequenceVerify(p), calcOpCount(opCount, OP_CHECKSEQUENCEVERIFY))
              } //if not, check to see if we should discourage p
              else if (ScriptFlagUtil.discourageUpgradableNOPs(p.flags)) {
                logger.error("We cannot execute a NOP when the ScriptVerifyDiscourageUpgradableNOPs is set")
                loop(ScriptProgram(p, ScriptErrorDiscourageUpgradableNOPs), calcOpCount(opCount, OP_CHECKSEQUENCEVERIFY))
              } //in this case, just read OP_CSV just like a NOP and remove it from the stack
              else loop(ScriptProgram(p, p.script.tail, ScriptProgram.Script), calcOpCount(opCount, OP_CHECKSEQUENCEVERIFY))
            //no more script operations to run, return whether the program is valid and the final state of the program
            case Nil => loop(ScriptProgram.toExecutedProgram(p), opCount)
            case h :: t => throw new RuntimeException(h + " was unmatched")
          }
      }
    }
  }

  /**
   * Checks the validity of a transaction in accordance to bitcoin core's CheckTransaction function
   * https://github.com/bitcoin/bitcoin/blob/f7a21dae5dbf71d5bc00485215e84e6f2b309d0a/src/main.cpp#L939.
   */
  def checkTransaction(transaction: Transaction): Boolean = {
    val inputOutputsNotZero = !(transaction.inputs.isEmpty || transaction.outputs.isEmpty)
    val txNotLargerThanBlock = transaction.bytes.size < Consensus.maxBlockSize
    val outputsSpendValidAmountsOfMoney = !transaction.outputs.exists(o =>
      o.value < CurrencyUnits.zero || o.value > Consensus.maxMoney)

    val outputValues = transaction.outputs.map(_.value)
    val totalSpentByOutputs: CurrencyUnit = outputValues.fold(CurrencyUnits.zero)(_ + _)
    val allOutputsValidMoneyRange = validMoneyRange(totalSpentByOutputs)
    val prevOutputTxIds = transaction.inputs.map(_.previousOutput.txId)
    val noDuplicateInputs = prevOutputTxIds.distinct.size == prevOutputTxIds.size

    val isValidScriptSigForCoinbaseTx = transaction.isCoinbase match {
      case true => transaction.inputs.head.scriptSignature.asmBytes.size >= 2 &&
        transaction.inputs.head.scriptSignature.asmBytes.size <= 100
      case false =>
        //since this is not a coinbase tx we cannot have any empty previous outs inside of inputs
        !transaction.inputs.exists(_.previousOutput == EmptyTransactionOutPoint)
    }
    inputOutputsNotZero && txNotLargerThanBlock && outputsSpendValidAmountsOfMoney && noDuplicateInputs &&
      allOutputsValidMoneyRange && noDuplicateInputs && isValidScriptSigForCoinbaseTx
  }

  /** Determines if the given currency unit is within the valid range for the system */
  def validMoneyRange(currencyUnit: CurrencyUnit): Boolean = {
    currencyUnit >= CurrencyUnits.zero && currencyUnit <= Consensus.maxMoney
  }

  /**  Calculates the new op count after the execution of the given [[ScriptToken]] */
  private def calcOpCount(oldOpCount: Int, token: ScriptToken): Int = BitcoinScriptUtil.countsTowardsScriptOpLimit(token) match {
    case true => oldOpCount + 1
    case false => oldOpCount
  }
}

object ScriptInterpreter extends ScriptInterpreter