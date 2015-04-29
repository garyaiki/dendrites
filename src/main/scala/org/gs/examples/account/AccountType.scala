/**
  */
package org.gs.examples.account

/** @author garystruthers
  *
  */
sealed trait AccountType
case object Checking extends AccountType
case object Savings extends AccountType
case object MoneyMarket extends AccountType

final case class GetCustomerAccountBalances(id: Long, accountTypes: Set[AccountType])
final case class GetAccountBalances(id: Long)

final case class AccountBalances(accountType: AccountType,
                                 balance: Option[List[(Long, BigDecimal)]])

final case class CheckingAccountBalances(balances: Option[List[(Long, BigDecimal)]])
final case class SavingsAccountBalances(balances: Option[List[(Long, BigDecimal)]])
final case class MoneyMarketAccountBalances(balances: Option[List[(Long, BigDecimal)]])


