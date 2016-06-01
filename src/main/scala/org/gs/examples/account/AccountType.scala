/**
  */
package org.gs.examples.account

import scala.reflect.runtime.universe._

/** Example objects for pattern matching and value objects
  *
  * @author Gary Struthers
  */
sealed trait AccountType extends Product
case object Checking extends AccountType
case object Savings extends AccountType
case object MoneyMarket extends AccountType

case class GetAccountBalances(id: Long)
case class GetCustomerAccountBalances(id: Long, accountTypes: Set[AccountType])
sealed trait AccountBalances extends Product
case class CheckingAccountBalances[A: TypeTag](balances: AccBalances[A]) extends AccountBalances
case class MoneyMarketAccountBalances[A: TypeTag](balances: AccBalances[A]) extends AccountBalances
case class SavingsAccountBalances[A: TypeTag](balances: AccBalances[A]) extends AccountBalances

