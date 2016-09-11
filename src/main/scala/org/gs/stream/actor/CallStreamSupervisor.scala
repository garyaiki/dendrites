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
package org.gs.stream.actor

import akka.NotUsed
import akka.actor.{Actor,
  ActorLogging,
  ActorRef,
  OneForOneStrategy,
  Props,
  Stash,
  SupervisorStrategy,
  Terminated}
import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, OverflowStrategy}
import akka.stream.OverflowStrategy.fail
import akka.stream.scaladsl.{Flow, RunnableGraph, SourceQueueWithComplete}
import java.util.MissingResourceException
import scala.concurrent.duration._
import scala.reflect.runtime.universe._

/** Creates CallStream Actor with a RunnablaGraph
  *
  *
  * @tparam A: TypeTag type passed to stream
  * @param initSinkActor: SinkActor case class with ActorRef, name
  * @author Gary Struthers
  */
class CallStreamSupervisor[A: TypeTag](rg: RunnableGraph[SourceQueueWithComplete[A]])
        extends Actor with Stash with ActorLogging {

  implicit val system = context.system
  implicit val ec = system.dispatcher
  implicit val logger = log
  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(system))
    
  val bufferSize = 10
  val overflowStrategy = OverflowStrategy.fail
  val callStreamName = "CallStream" + typeOf[A].getClass.getSimpleName
  var callStream: ActorRef = null
  
  override def preStart() = {
    log.debug("preStart {} callStream:{}", this.toString(), callStreamName)    
    //create children here
    val props = CallStream.props[A](rg)

    callStream = context.actorOf(props, callStreamName)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "preRestart due to [{}] when processing [{}]",
        reason.getMessage, message.getOrElse(""))
    super.preRestart(reason, message) // stops children
  }

  /** SupervisorStrategy for child actors. Non-escalating errors are logged, these logs should guide
    * how to handle exceptions. case t invokes the default strategy for unnamed exceptions.
    * @see [[http://doc.akka.io/docs/akka/current/scala/fault-tolerance.html fault-tolerance]]
    */
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1.minute) {
      case _: MissingResourceException => Restart
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: IllegalStateException => Restart
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => SupervisorStrategy.Escalate)
    }

  def receive = {
        case x: A ⇒ callStream forward x
  }
}

object CallStreamSupervisor {

  def props[A: TypeTag](rg: RunnableGraph[SourceQueueWithComplete[A]]): Props =
    Props(new CallStreamSupervisor[A](rg))
}

