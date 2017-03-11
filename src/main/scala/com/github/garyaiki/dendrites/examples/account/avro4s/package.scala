/**

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
