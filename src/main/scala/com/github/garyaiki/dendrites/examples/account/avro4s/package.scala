package com.github.garyaiki.dendrites.examples.account
import scala.reflect.runtime.universe.TypeTag

package object avro4s {
  case class Balance(id: Long, amount: BigDecimal)
  case class CheckingAccountBalances(balances: Seq[Balance])
  case class CheckingAccountOptBalances(balances: Option[Seq[Balance]])

}