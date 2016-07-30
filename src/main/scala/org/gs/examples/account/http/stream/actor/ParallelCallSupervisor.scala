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
  OneForOneStrategy,
  Props,
  Stash,
  SupervisorStrategy,
  Terminated}
import akka.http.scaladsl.model.{EntityStreamException,
          EntityStreamSizeException,
          IllegalHeaderException,
          IllegalRequestException,
          IllegalResponseException,
          IllegalUriException,
          InvalidContentLengthException,
          ParsingException,
          RequestTimeoutException}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, OverflowStrategy}
import akka.stream.OverflowStrategy.fail
import akka.stream.scaladsl.Flow
import java.util.MissingResourceException
import scala.concurrent.duration._
import scala.reflect.runtime.universe._
import org.gs.examples.account.http.stream.ParallelCallFlow
import org.gs.stream.actor.{CallStream, OtherActor}
import org.gs.stream.actor.StreamSinkRefSupervisor.SinkActor

/** Creates CallStream with a RunnablaGraph for ParallelCallFlow
  *
  * Watches Sink actor and changes state to waiting when it dies
  *
  * @tparam A case class or tuple type passed to stream
  * @param sinkActor actorRef for Sink
  * @author Gary Struthers
  */
class ParallelCallSupervisor[A <: Product: TypeTag](initSinkActor: SinkActor) extends
        Actor with Stash with ActorLogging {

  implicit val system = context.system
  implicit val logger = log
  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(system))
    
  val pcf = new ParallelCallFlow
  val wrappedFlow: Flow[A, (Seq[String], Seq[AnyRef]), NotUsed] = pcf.wrappedCallsLRFlow
  val bufferSize = 10
  val overflowStrategy = OverflowStrategy.fail
  var sinkActor: SinkActor = initSinkActor
  val callStreamName = "CallStream"// + typeOf[A].getClass.getSimpleName
  var callStream: ActorRef = null
  
  /** Create a CallStream Actor. First watch the ActorRef of the Sink's target.
    *
  	* @param sinkRef ActorRef passed to Sink
  	* @return CallStream ActorRef
  	*/
  def createCallStream(ref: ActorRef): ActorRef = {
    val props = CallStream.props[A](ref, wrappedFlow)
    context.watch(ref)    
    context.actorOf(props, callStreamName)
  }

  override def preStart() = {
    log.debug("preStart {}", this.toString())    
    //create children here
    callStream = createCallStream(sinkActor.ref)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
        reason.getMessage, message.getOrElse(""))
    context.unwatch(sinkActor.ref)
    super.preRestart(reason, message) // stops children
  }

  /** SupervisorStrategy for child actors. Non-escalating errors are logged, these logs should guide
    * how to handle exceptions. case t invokes the default strategy for unnamed exceptions.
    * @see [[http://doc.akka.io/docs/akka/current/scala/fault-tolerance.html fault-tolerance]]
    */
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1.minute) {
      case _: EntityStreamSizeException => SupervisorStrategy.Resume
      case _: InvalidContentLengthException => SupervisorStrategy.Resume
      case _: MissingResourceException => SupervisorStrategy.Resume
      case _: RequestTimeoutException => SupervisorStrategy.Resume
      case _: EntityStreamException => SupervisorStrategy.Restart
      case _: NullPointerException => SupervisorStrategy.Restart
      case _: IllegalArgumentException => SupervisorStrategy.Stop
      case _: IllegalHeaderException => SupervisorStrategy.Stop
      case _: IllegalRequestException => SupervisorStrategy.Stop
      case _: IllegalResponseException => SupervisorStrategy.Stop
      case _: IllegalUriException => SupervisorStrategy.Stop
      case _: ParsingException => SupervisorStrategy.Stop
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => SupervisorStrategy.Escalate)
    }


  /** ready:Receive normal processing
    *
    * Forward messages intended for CallStream
    *  
    * Watched actors send Terminated msg when they die. Unwatch it, stop the child that uses it, ask
    * parent supervisor for replacement actor. Then swap actor to waiting
    *
  	*
  	* @see [[http://doc.akka.io/api/akka/current/#akka.actor.Terminated Terminated]]
  	*/
  def ready: Receive = {
    case x: A ⇒ callStream forward x

    case Terminated(actor) ⇒ {
      context.parent ! SinkActor(null, sinkActor.name)
      context.stop(callStream)
      context.unwatch(sinkActor.ref)
      context.become(waiting)
      log.warning("sinkActor {} terminated", sinkActor)
    }
  }

  /** waiting Receive state.
    *
    * Stash messages intended for CallStream
    *  
    * Watched actors send Terminated msg when they die. Unwatch it, ask parent supervisor for
    * replacement actor.
    *
    * When parent sends a new actor to watch, createCallStream watches it, creates a new
    * CallStream actor, unStashes all messages for CallStream
  	*
  	* @see [[http://doc.akka.io/api/akka/current/#akka.actor.Terminated Terminated]]
  	*/
  def waiting: Receive = {
    case x: A ⇒ stash()

    case Terminated(actor) ⇒ {
      context.parent ! SinkActor(null, sinkActor.name)
      context.unwatch(sinkActor.ref)
      log.warning("sinkActor {} terminated while not ready", sinkActor)
    }

    case newSink: SinkActor => {
      sinkActor = newSink
      callStream = createCallStream(newSink.ref)
      unstashAll()
      context.become(ready)
    }    
  }

  /** receive is ready for normal processing, waiting while updating ActorRef for Sink	*/
  def receive = ready

}

object ParallelCallSupervisor {
  //case class SinkActor(override val ref: ActorRef, override val name: String) extends OtherActor  

  def props[A <: Product: TypeTag](sinkActor: SinkActor): Props = Props(new ParallelCallSupervisor[A](sinkActor))
}

