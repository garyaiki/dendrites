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
package org.gs.algebird.agent

import com.twitter.algebird.QTreeSemigroup
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import org.gs._
import org.gs.algebird.{AlgebirdConfigurer, BigDecimalField}
import org.gs.fixtures.TestValuesBuilder
import org.gs.algebird.typeclasses.QTreeLike

/**
  *
  * @author Gary Struthers
  *
  */
class QTreeAgentSpec extends WordSpecLike with Matchers with TestValuesBuilder {
  val timeout = Timeout(3000 millis)
  val level = AlgebirdConfigurer.qTreeLevel
  implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](level)

  "A QTreeAgent of BigDecimal" should {
    "have count of 1 when initialized without data" in {
      val qTreeAgent = new QTreeAgent[BigDecimal]("testBD zero")
      val a = qTreeAgent.agent.get()
      a.count shouldBe 1
    }
    "have the count of first update data" in {
      val qTreeAgent = new QTreeAgent[BigDecimal]("testBD 1st update data")
      val updateFuture = qTreeAgent.alter(bigDecimals)
      whenReady(updateFuture, timeout) { result =>
        result.count shouldBe bigDecimals.size
      }
    }
    val qTreeAgent = new QTreeAgent[BigDecimal]("testBD bounds", level, Some(bigDecimals))
    val qTree = qTreeAgent.agent.get()
    val lb = qTree.lowerBound
    "have a lower bound" in {
      lb <= bigDecimals.min
    }
    val ub = qTree.upperBound
    "have a upper bound" in {
      ub >= bigDecimals.max
    }
    "have 1st quantile bounds" in {
      val fst = qTree.quantileBounds(0.25)
      val q1 = 103.0
      fst._1 >= q1
      fst._2 <= q1 + 0.0001
    }
    "have 2nd quantile bounds" in {
      val snd = qTree.quantileBounds(0.5)
      val q2 = 110.0
      snd._1 >= q2
      snd._2 <= q2 + 0.0001
    }
    "have 3rd quantile bounds" in {
      val trd = qTree.quantileBounds(0.75)
      val q3 = 116.0
      trd._1 >= q3
      trd._2 <= q3 + 0.0001
    }
    "have range sum bounds" in {
      val rsb = qTree.rangeSumBounds(lb, ub)
      val sum = bigDecimals.sum
      rsb shouldBe (sum, sum)
    }
    "have range count bounds" in {
      val rcb = qTree.rangeCountBounds(lb, ub)
      val size = bigDecimals.size
      rcb shouldBe (size, size)
    }
  }
}
