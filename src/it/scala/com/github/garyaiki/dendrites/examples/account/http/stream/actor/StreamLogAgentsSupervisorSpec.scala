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
package com.github.garyaiki.dendrites.examples.account.http.stream.actor

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import com.twitter.algebird.{AveragedValue, QTreeSemigroup}
import com.twitter.algebird.CMSHasherImplicits._
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.time.SpanSugar._
import scala.reflect.runtime.universe.TypeTag
import com.github.garyaiki.dendrites.aggregator.mean
import com.github.garyaiki.dendrites.algebird.{AlgebirdConfigurer, BigDecimalField}
import com.github.garyaiki.dendrites.algebird.cmsHasherBigDecimal
import com.github.garyaiki.dendrites.algebird.agent.Agents
import com.github.garyaiki.dendrites.algebird.typeclasses.{HyperLogLogLike, QTreeLike}
import com.github.garyaiki.dendrites.examples.account.{CheckingAccountBalances, GetAccountBalances,
  GetCustomerAccountBalances, Savings}
import com.github.garyaiki.dendrites.examples.account.http.{BalancesProtocols, CheckingBalancesClientConfig}
import com.github.garyaiki.dendrites.http.{caseClassToGetQuery, typedQueryResponse}

/** Parent Supervisor creates child supervisor of parallel HTTP stream and actor with parallel
  * Agents stream
  *
  * @note Http connection pool may not be initialized, so a dummy call is made in before.
  * @note Balances server has the default 4 max connections, so Thread.sleep is called after request
  * @note Thread.sleep may be called if agent hasn't been updated due to contention from other tests
  */
class StreamLogAgentsSupervisorSpec extends WordSpecLike with Matchers with BeforeAndAfter with BalancesProtocols {

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
    val id = 1L
    val clientConfig = new CheckingBalancesClientConfig()
    val baseURL = clientConfig.baseURL
    def partial = typedQueryResponse(baseURL, "GetAccountBalances", caseClassToGetQuery, mapPlain, mapChecking) _

    val responseFuture = partial(GetAccountBalances(id))

    whenReady(responseFuture, Timeout(60000 millis)) { result => }
  }

  "A StreamLogAgentsSupervisor of BigDecimals" should {
    "update all agents and return their latest values" in {
      val level = AlgebirdConfigurer.qTreeLevel
      implicit val qtSG = new QTreeSemigroup[BigDecimal](level)
      agents = new Agents[BigDecimal]("test BigDecimal approximators agents")
      val props = StreamLogAgentsSupervisor.props[BigDecimal](agents)
      val supervisor = system.actorOf(props)
      supervisor ! GetCustomerAccountBalances(1, Set(Savings))
      Thread.sleep(20000)//tests complete before agent updates
      val avgAgent = agents.avgAgent
      whenReady(avgAgent.agent.future(), futureTimeout) { result =>
        logger.debug("StreamLogAgents test 1st try"+ java.util.Calendar.getInstance().getTime())
        var count = result.count
        if(count > 0) testAvgVal(result) else {
          Thread.sleep(60000)//tests complete before agent updates
          whenReady(avgAgent.agent.future(), futureTimeout) { result =>
            logger.debug("StreamLogAgents test 2nd try"+ java.util.Calendar.getInstance().getTime())
            testAvgVal(result)
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
      whenReady(updateQTFuture, futureTimeout) { result => result.count should equal(3)}
    }
  }
}
