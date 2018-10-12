package org.bitcoins.core.wallet.fee

import org.bitcoins.core.currency.{ CurrencyUnit, Satoshis }
import org.bitcoins.core.number.Int64
import org.bitcoins.core.protocol.transaction.Transaction

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

