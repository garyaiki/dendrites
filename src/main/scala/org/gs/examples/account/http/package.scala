
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
