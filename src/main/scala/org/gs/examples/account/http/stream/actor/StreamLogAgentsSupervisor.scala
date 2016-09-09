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

import akka.NotUsed
import akka.actor.{Actor,
  ActorLogging,
  ActorRef,
  ActorSystem,
  Props,
  OneForOneStrategy,
  SupervisorStrategy}
import akka.event.LoggingAdapter
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Sink, Source}
import com.twitter.algebird.{CMSHasher, DecayedValue, DecayedValueMonoid, HLL, QTreeSemigroup}
import com.twitter.algebird.CMSHasherImplicits._
import scala.concurrent.duration._
import scala.reflect.runtime.universe.TypeTag
//import org.gs.actor.LogLeftSendRightActor
//import org.gs.actor.LogLeftSendRightActor._
import org.gs.algebird.BigDecimalField
import org.gs.algebird.cmsHasherBigDecimal
import org.gs.algebird.agent.Agents
import org.gs.algebird.agent.stream.ParallelApproximators
import org.gs.algebird.agent.stream.DecayedValueAgentFlow.nowMillis
import org.gs.algebird.agent.stream.ParallelApproximators._
import org.gs.algebird.typeclasses.{HyperLogLogLike, QTreeLike}
import org.gs.examples.account.GetAccountBalances
import org.gs.examples.account.stream.extractBalancesFlow
import org.gs.stream.actor.CallStream
import org.gs.stream.actor.CallStream.props
import org.gs.stream.actor.OtherActor
import ParallelCallSupervisor.props
import ParallelCallSupervisor.SinkActor
import StreamLogAgentsSupervisor.ResultsActor

/** Creates ParallelCallSupervisor, LogLeftSendRightActor, ResultsActor
  *
  *	Results Runnable Graph
  * {{{
  *																								agentsFlow
  *  																			  bcast ~> avg ~> zip.in0
  * 																				bcast ~> cms ~> zip.in1
  * sourceQueue ~> 	extractBalancesFlow ~>  bcast ~> dvt ~> zip.in2 ~> sink
  * 																				bcast ~> hll ~> zip.in3
  * 																				bcast ~> qtrAg ~> zip.in4
  * }}}
  *	@constructor creates RunnableGraph for results then creates CallStream child actor for it
  * @tparam A: CMSHasher: HyperLogLogLike: Numeric: QTreeLike: TypeTag
  * @param agents Algebird approximator agents
  * @param system implicit ActorSystem
  * @param logger implicit LoggingAdapter
  * @param materializer Materializer
  * @see [[http://doc.akka.io/docs/akka/current/general/supervision.html supervision]]
  * @author Gary Struthers
  *
  */
class StreamLogAgentsSupervisor[A: CMSHasher: HyperLogLogLike: Numeric: QTreeLike: TypeTag]
        (agents: Agents[A])
        (implicit val system: ActorSystem, logger: LoggingAdapter, val materializer: Materializer)
        extends Actor with ActorLogging {

  val agentsFlow = ParallelApproximators.compositeFlow(agents.avgAgent,
      agents.cmsAgent,
      agents.dcaAgent,
      agents.hllAgent,
      agents.qtAgent,
      nowMillis[A])
  val source = Source.queue[Seq[AnyRef]](10, OverflowStrategy.fail)
  val resultsRunnable = source.map { elem => log.debug("Source queue elem{}", elem); elem }
  .via(extractBalancesFlow)
  .via(agentsFlow)
  .to(Sink.ignore)

      
  val errorLoggerName = "errorLogger"
  var errorLogger: ActorRef = null
  val streamSuperName = "streamSuper"
  var streamSuper: ActorRef = null
  val resultsName = "streamResults"
  var results: ActorRef = null

  override def preStart() = {
    //create children here
    val resultsProps = CallStream.props[Seq[AnyRef]](resultsRunnable)
    results = context.actorOf(resultsProps, resultsName)
    val sinkActor = SinkActor(results, resultsName)
    val superProps = ParallelCallSupervisor.props[GetAccountBalances](sinkActor)
    streamSuper = context.actorOf(superProps, streamSuperName)
    log.debug("preStart {}", this.toString())
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message) // stops children
    log.error(reason, "Restarting due to [{}] when processing [{}]",
        reason.getMessage, message.getOrElse(""))
  }

  /** SupervisorStrategy for child actors. Non-escalating errors are logged, these logs should guide
    * how to handle exceptions. case t invokes the default strategy for unnamed exceptions.
    * @see [[http://doc.akka.io/docs/akka/current/scala/fault-tolerance.html fault-tolerance]]
    */
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1.minute) {
      case _: NullPointerException => SupervisorStrategy.Stop
      case _: IllegalArgumentException => SupervisorStrategy.Stop
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => SupervisorStrategy.Escalate)
    }

  def receive = {
    case ResultsActor(_, resultsName) => sender ! ResultsActor(results, resultsName)
    case SinkActor(_, errorLoggerName) ⇒ sender ! SinkActor(errorLogger, errorLoggerName)
  }
}

object StreamLogAgentsSupervisor {
  case class ResultsActor(override val ref: ActorRef, override val name: String) extends OtherActor

  def props[A: CMSHasher: HyperLogLogLike: Numeric: QTreeLike: TypeTag](agents: Agents[A])
          (implicit system: ActorSystem, logger: LoggingAdapter, materializer: Materializer) =
    Props(new StreamLogAgentsSupervisor[A](agents))
}
