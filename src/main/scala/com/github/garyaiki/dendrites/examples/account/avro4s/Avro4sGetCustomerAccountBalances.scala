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
package com.github.garyaiki.dendrites.examples.account.avro4s

import scala.collection.mutable.ArrayBuffer
import com.github.garyaiki.dendrites.examples.account.{AccountType, Checking, MoneyMarket, GetCustomerAccountBalances,
  Savings}

/** GetCustomerAccountBalances Avro4s serialization and deserialization.
  *
  * Set[AccountType] doesn't map to Avro, so use GetCustomerStringAccountBalances with Avro4s
  *
  * @author Gary Struthers
  */
object Avro4sGetCustomerAccountBalances {

  val avroFriendly = Avro4sGetCustomerStringAccountBalances

  /** Map Set[AccountType] to Set[String]
    *
    * @param from Set[AccountType]
    * @return Set[String]
    */
  def toStrings(from: Set[AccountType]): Set[String] = {
    val ab = new ArrayBuffer[String]
    from foreach { x => ab += x.productPrefix }
    ab.toSet
  }

  /** Copy GetCustomerAccountBalances to Avro friendly GetCustomerStringAccountBalances
    *
    * @param from GetCustomerAccountBalances
    * @return GetCustomerStringAccountBalances
    */
  def toAvroFriendly(from: GetCustomerAccountBalances): GetCustomerStringAccountBalances = {
    GetCustomerStringAccountBalances(from.id, toStrings(from.accountTypes))
  }

  /** GetCustomerAccountBalances to Array[Byte]
    *
    * @param caseClass
    * @return Array[Byte]
    */
  def toBytes(cc: GetCustomerAccountBalances): Array[Byte] = {
    val caseClass = toAvroFriendly(cc)
    avroFriendly.toBytes(caseClass)
  }

  /** Map Set[String] to Set[AccountType]
    *
    * @param from Set[String]
    * @return Set[AccountType]
    */
  def fromStrings(from: Set[String]): Set[AccountType]= {
    val ab = new ArrayBuffer[AccountType]
    from foreach {x => x match {
      case "Checking" => ab += Checking
      case "MoneyMarket" => ab += MoneyMarket
      case "Savings" => ab += Savings
    }}
    ab.toSet
  }

  /** Copy Avro friendly GetCustomerStringAccountBalances to GetCustomerAccountBalances
    *
    * @param from GetCustomerStringAccountBalances
    * @return GetCustomerAccountBalances
    */
  def fromAvroFriendly(from: GetCustomerStringAccountBalances): GetCustomerAccountBalances = {
    GetCustomerAccountBalances(from.id, fromStrings(from.accountTypes))
  }

  /** Array[Byte] to GetCustomerAccountBalances
    *
    * @param bytes
    * @return GetAccountBalances
    */
  def toCaseClass(bytes: Array[Byte]): GetCustomerAccountBalances = {
    val caseClass = avroFriendly.toCaseClass(bytes)
    fromAvroFriendly(caseClass)
  }
}
