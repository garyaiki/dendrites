/**
  */
package org.gs.examples.account

/** @author garystruthers
  *
  */
sealed trait AccountType extends Product
case object Checking extends AccountType
case object Savings extends AccountType
case object MoneyMarket extends AccountType

final case class GetAccountBalances(id: Long)
case class GetCustomerAccountBalances(id: Long, accountTypes: Set[AccountType])
sealed trait AccountBalances extends Product
case class CheckingAccountBalances[A](balances: AccBalances[A]) extends AccountBalances
case class MoneyMarketAccountBalances[A](balances: AccBalances[A]) extends AccountBalances
case class SavingsAccountBalances[A](balances: AccBalances[A]) extends AccountBalances

