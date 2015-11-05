/**
  */
package org.gs.examples.fixtures

import org.gs.examples.account._
import scala.collection.immutable.Range
import scala.math.Numeric.BigDecimalAsIfIntegral
import org.scalatest._
import scala.annotation.implicitNotFound

/** @author garystruthers
  *
  */
trait AccountTypesBuilder extends SuiteMixin { this: Suite =>

  abstract override def withFixture(test: NoArgTest): Outcome = {
    super.withFixture(test)
  }

  val idRange = 1L until 9L
  implicit val bd = BigDecimalAsIfIntegral
  val start: BigDecimal = BigDecimal.decimal(1000.0)
  val end: BigDecimal = BigDecimal.decimal(1500.0)
  val step: BigDecimal = BigDecimal.decimal(90.0)
  val balancesRange = Range.BigDecimal(start, end, step)
  val acTypes = accountTypes.toIndexedSeq

  def makeBalances(): IndexedSeq[(Long, BigDecimal)] = {
    for {
      id <- idRange
      balances <- balancesRange
    } yield (id, balances)
  }
  
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

  val accountBalances: IndexedSeq[AccBalances[BigDecimal]] = makeAccountBalances()
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
