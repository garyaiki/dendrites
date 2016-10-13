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
package org.gs.examples.account.http.stream.actor

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.twitter.algebird.{AveragedValue, CMSHasher, DecayedValue, DecayedValueMonoid, HLL, QTreeSemigroup}
import com.twitter.algebird.CMSHasherImplicits._
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.reflect.runtime.universe.TypeTag
import org.gs.aggregator.mean
import org.gs.algebird.AlgebirdConfigurer
import org.gs.algebird.BigDecimalField
import org.gs.algebird.cmsHasherBigDecimal
import org.gs.algebird.agent.Agents
import org.gs.algebird.typeclasses.{HyperLogLogLike, QTreeLike}
import org.gs.examples.account.{GetCustomerAccountBalances, Savings}

/** Parent Supervisor creates child supervisor of parallel HTTP stream and actor with parallel
  * Agents stream
  */
class StreamLogAgentsSupervisorSpec extends WordSpecLike with Matchers with BeforeAndAfter {

  implicit val system = ActorSystem("dendrites")
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val futureTimeout = Timeout(200 millis)
  var agents: Agents[BigDecimal] = null

  def testAvgVal(avgVal: AveragedValue): Unit = {
		  val count = avgVal.count
				  count should equal(3)
				  val dValue: Double = avgVal.value
				  val mD = mean(List[BigDecimal](1000.1, 11000.1, 111000.1))
				  val expectMean = mD.right.get.toDouble
				  dValue should be (expectMean  +- 0.1)        
  }

  before {
      val level = AlgebirdConfigurer.qTreeLevel
      implicit val qtSG = new QTreeSemigroup[BigDecimal](level)
      agents = new Agents[BigDecimal]("test BigDecimal approximators agents")
      val props = StreamLogAgentsSupervisor.props[BigDecimal](agents)
      val supervisor = system.actorOf(props)
      supervisor ! GetCustomerAccountBalances(1, Set(Savings))
      Thread.sleep(8000)//tests complete before agent updates
  }

  "A StreamLogAgentsSupervisor of BigDecimals" should {
    "update all agents and return their latest values" in {
      val avgAgent = agents.avgAgent
      whenReady(avgAgent.agent.future(), futureTimeout) { result =>
        var count = result.count
        if(count > 0) testAvgVal(result) else {
          Thread.sleep(8000)//tests complete before agent updates
          whenReady(avgAgent.agent.future(), futureTimeout) { result =>
            count = result.count
            if(count > 0) testAvgVal(result) else {
              Thread.sleep(8000)//tests complete before agent updates
              whenReady(avgAgent.agent.future(), futureTimeout) { result =>
                count = result.count
                testAvgVal(result)
              }
            }
          }
        }
      }
      val cmsAgent = agents.cmsAgent
      val updateCMSFuture = cmsAgent.agent.future()
      whenReady(updateCMSFuture, futureTimeout) { result =>
        val totalCount = result.totalCount
        totalCount should equal(3)
      }
      val dvAgent = agents.dcaAgent
      val updateDVFuture = dvAgent.agent.future()
      whenReady(updateDVFuture, futureTimeout) {  result =>
        val size = result.size
        size should be (3 +- 2)
      }
      val hllAgent = agents.hllAgent
      val updateHLLFuture = hllAgent.agent.future()
      whenReady(updateHLLFuture, futureTimeout) { result =>
        val estimatedSize = result.estimatedSize
        estimatedSize should be(3.0 +- 0.09)
      }
      val qtAgent = agents.qtAgent
		  val updateQTFuture = qtAgent.agent.future()
		  whenReady(updateQTFuture, futureTimeout) { result =>
		    result.count should equal(3)
		  }
    }
  }
}

