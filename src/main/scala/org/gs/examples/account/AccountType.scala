/** Copyright 2016 Gary Struthers

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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

