package com.github.garyaiki.dendrites.examples.account

/** Avro4s objects, functions, case classes for account examples
  *
  * Avro4sOps trait is extended by a concrete object or class to serialize/deserialize
  * {{{
  * import Avro4sXXX.{schemaFor, toBytes, toCaseClass} // extends Avro4sOps
  * val caseClass = XXX
  * val bytes = toBytes(caseClass)
  * val deSerCaseClass = toCaseClass(bytes)
  * }}}
  */
package object avro4s {
  case class Balance(id: Long, amount: BigDecimal)
  case class CheckingAccountBalances(balances: Seq[Balance])
  case class CheckingAccountOptBalances(balances: Option[Seq[Balance]])
  case class GetCustomerStringAccountBalances(id: Long, accountTypes: Set[String])
}