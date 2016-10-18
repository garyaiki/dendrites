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

/** Mock functions called by BalancesService
  *
  * Get dummy CheckingBalances
  * {{{
  * def fetchCheckingBalances(id: Long): Either[String, CheckingAccountBalances[BigDecimal]] = {
  *   checkingBalances.get(id) match {
  *     case Some(x) => Right(CheckingAccountBalances(x))
  *     case None    => Left(s"Checking account $id not found")
  *     case _       => Left(s"Error looking up checking account $id")
  *   }
  * }
  * }}}
  * @author Gary Struthers
  *
  */
package object http {

  val checkingBalances = Map(
    1L -> Some(List(
      (1L, BigDecimal(1000.10)))),
    2L -> Some(List(
      (2L, BigDecimal(2000.20)),
      (22L, BigDecimal(2200.22)))),
    3L -> Some(List(
      (3L, BigDecimal(3000.30)),
      (33L, BigDecimal(3300.33)),
      (333L, BigDecimal(3330.33)))))

  val moneyMarketBalances = Map(
    1L -> Some(List(
      (1L, BigDecimal(11000.10)))),
    2L -> Some(List(
      (2L, BigDecimal(22000.20)),
      (22L, BigDecimal(22200.22)))),
    3L -> Some(List(
      (3L, BigDecimal(33000.30)),
      (33L, BigDecimal(33300.33)),
      (333L, BigDecimal(33330.33)))))

  val savingsBalances = Map(
    1L -> Some(List(
      (1L, BigDecimal(111000.10)))),
    2L -> Some(List(
      (2L, BigDecimal(222000.20)),
      (22L, BigDecimal(222200.22)))),
    3L -> Some(List(
      (3L, BigDecimal(333000.30)),
      (33L, BigDecimal(333300.33)),
      (333L, BigDecimal(333330.33)))))
}
