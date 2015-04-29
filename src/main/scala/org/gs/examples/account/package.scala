/**
  */
package org.gs.examples

import scala.collection.Set
/** @author garystruthers
  *
  */
package object account {

  type AccBalances = (AccountType, Option[List[(Long, BigDecimal)]])
  def AccBalancesVec(xs: AccBalances*) = Vector(xs: _*)
  val accountTypes: Set[AccountType] = Set(Checking, Savings, MoneyMarket)

  def isAccBalances(e: Any): Boolean = {
    println(s"e:$e")
    e.isInstanceOf[Option[List[(Long, BigDecimal)]]]
  }
  def isAccountType(e: Any): Boolean = e.isInstanceOf[AccountType]


}