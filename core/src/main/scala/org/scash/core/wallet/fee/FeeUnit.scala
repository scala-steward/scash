package org.scash.core.wallet.fee

import org.scash.core.currency.Satoshis
import org.scash.core.protocol.transaction.Transaction
import org.scash.core.currency.{ CurrencyUnit, Satoshis }
import org.scash.core.number.Int64

/**
 * This is meant to be an abstract type that represents different fee unit measurements for
 * blockchains
 */
sealed abstract class FeeUnit {
  def currencyUnit: CurrencyUnit
  def *(tx: Transaction): CurrencyUnit = calc(tx)
  def calc(tx: Transaction): CurrencyUnit = Satoshis(Int64(tx.size * toLong))
  def toLong: Long = currencyUnit.satoshis.toLong
}

/**
 * Meant to represent the different fee unit types for the bitcoin protocol
 * [[https://en.bitcoin.it/wiki/Weight_units]]
 */
sealed abstract class BitcoinFeeUnit extends FeeUnit

case class SatoshisPerByte(currencyUnit: CurrencyUnit) extends BitcoinFeeUnit

