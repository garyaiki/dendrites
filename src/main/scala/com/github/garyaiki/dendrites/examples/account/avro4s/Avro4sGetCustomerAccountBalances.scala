package com.github.garyaiki.dendrites.examples.account.avro4s

import scala.collection.mutable.ArrayBuffer
import com.github.garyaiki.dendrites.examples.account.{AccountType, Checking, MoneyMarket, GetCustomerAccountBalances,
  GetCustomerStringAccountBalances, Savings}

/** Functions for Avro serialization and deserialization
  *
  * accountTypes: Set[AccountType] doesn't map to Avro, so use GetCustomerStringAccountBalances with
  * accountTypes: Seq[String]
  *
  * @author Gary Struthers
  */
object Avro4sGetCustomerAccountBalances {

  val avroFriendly = Avro4sGetCustomerStringAccountBalances

  def toStrings(from: Set[AccountType]): Set[String] = {
    val ab = new ArrayBuffer[String]
    from foreach { x => ab += x.productPrefix }
    ab.toSet
  }

  def toAvroFriendly(from: GetCustomerAccountBalances): GetCustomerStringAccountBalances = {
    GetCustomerStringAccountBalances(from.id, toStrings(from.accountTypes))
  }

  def toBytes(cc: GetCustomerAccountBalances): Array[Byte] = {
    val caseClass = toAvroFriendly(cc)
    avroFriendly.toBytes(caseClass)
  }

  def fromStrings(from: Set[String]): Set[AccountType]= {
    val ab = new ArrayBuffer[AccountType]
    from foreach {x => x match {
      case "Checking" => ab += Checking
      case "MoneyMarket" => ab += MoneyMarket
      case "Savings" => ab += Savings
    }}
    ab.toSet
  }

  def fromAvroFriendly(from: GetCustomerStringAccountBalances): GetCustomerAccountBalances = {
    GetCustomerAccountBalances(from.id, fromStrings(from.accountTypes))
  }

  def toCaseClass(bytes: Array[Byte]): GetCustomerAccountBalances = {
    val caseClass = avroFriendly.toCaseClass(bytes)
    fromAvroFriendly(caseClass)
  }
}