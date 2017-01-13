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
package com.github.garyaiki.dendrites.examples.fixtures

import scala.collection.immutable.Range
import scala.math.Numeric.BigDecimalAsIfIntegral
import org.scalatest.{Outcome, TestSuite, TestSuiteMixin}
import scala.annotation.implicitNotFound
import com.github.garyaiki.dendrites.examples.account.AccBalances
import com.github.garyaiki.dendrites.examples.account.AccountType
import com.github.garyaiki.dendrites.examples.account.accountTypes

/**
  * @author Gary Struthers
  */
trait AccountTypesBuilder extends TestSuiteMixin { this: TestSuite =>

  abstract override def withFixture(test: NoArgTest): Outcome = { super.withFixture(test) }

  val idRange = 1L until 9L
  implicit val bd = BigDecimalAsIfIntegral
  val start: BigDecimal = BigDecimal.decimal(1000.0)
  val end: BigDecimal = BigDecimal.decimal(1500.0)
  val step: BigDecimal = BigDecimal.decimal(90.0)
  val balancesRange = Range.BigDecimal(start, end, step)
  val acTypes = accountTypes.toIndexedSeq

  def makeBalances(): IndexedSeq[(Long, BigDecimal)] = for {
      id <- idRange
      balances <- balancesRange
    } yield (id, balances)

  def makeAccountBalances(): IndexedSeq[AccBalances[BigDecimal]] = {
    def applyType(): AccountType = {
      import scala.util.Random
      Random.shuffle(acTypes).head
    }

    val b = makeBalances()
    val b3 = b.grouped(4)
    val accs = for (i <- b3) yield {
      val xs = i.toList
      Some(xs)
    }
    accs.toIndexedSeq
  }

  val accountBalances: IndexedSeq[AccBalances[BigDecimal]] = makeAccountBalances
  val accIdBals = accountBalances.flatten // Can't directly flatten Vector of List
  val accIds = for {
    i <- accIdBals
    j <- i
  } yield j._1

  val accVals = for {
    i <- accIdBals
    j <- i
  } yield j._2
}
