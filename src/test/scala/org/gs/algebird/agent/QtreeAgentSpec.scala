/**
  */
package org.gs.algebird.agent

import com.twitter.algebird._
import org.gs._
import org.gs.algebird._
import org.gs.algebird.typeclasses.QTreeLike
import org.gs.fixtures.TestValuesBuilder
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global

/** @author garystruthers
  *
  */
class QTreeAgentSpec extends WordSpecLike with Matchers with TestValuesBuilder {
  val timeout = Timeout(3000 millis)
  val level = 16

  "A QTreeAgent of BigDecimal" should {
    implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](level)
    "have count of 1 when initialized without data" in {
      val qTreeAgent = new QTreeAgent[BigDecimal]("testBD zero")
      val a = qTreeAgent.agent.get()
      a.count should equal(1)
    }
    "have the count of first update data" in {
      val qTreeAgent = new QTreeAgent[BigDecimal]("testBD 1st update data")
      val updateFuture = qTreeAgent.update(bigDecimals)
      whenReady(updateFuture, timeout) { result =>
        result.count should equal(bigDecimals.size)
      }
    }
    val qTreeAgent = new QTreeAgent[BigDecimal]("testBD bounds", level, Some(bigDecimals))
    val qTree = qTreeAgent.agent.get()
    val lb = qTree.lowerBound
    "have a lower bound" in {
      assert(lb <= bigDecimals.min)
    }
    val ub = qTree.upperBound
    "have a upper bound" in {
      assert(ub >= bigDecimals.max)
    }
    "have 1st quantile bounds" in {
      val fst = qTree.quantileBounds(0.25)
      val q1 = 102.0
      assert(fst._1 >= q1)
      assert(fst._2 <= q1 + 0.0001)
    }
    "have 2nd quantile bounds" in {
      val snd = qTree.quantileBounds(0.5)
      val q2 = 109.0
      assert(snd._1 >= q2)
      assert(snd._2 <= q2 + 0.0001)
    }
    "have 3rd quantile bounds" in {
      val trd = qTree.quantileBounds(0.75)
      val q3 = 115.0
      assert(trd._1 >= q3)
      assert(trd._2 <= q3 + 0.0001)
    }
    "have range sum bounds" in {
      val rsb = qTree.rangeSumBounds(lb, ub)
      val sum = bigDecimals.sum
      assert(rsb === (sum, sum))
    }
    "have range count bounds" in {
      val rcb = qTree.rangeCountBounds(lb, ub)
      val size = bigDecimals.size
      assert(rcb === (size, size))
    }
  }
}
