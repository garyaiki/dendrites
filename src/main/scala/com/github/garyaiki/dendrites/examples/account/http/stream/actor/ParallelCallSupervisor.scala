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
package com.github.garyaiki.dendrites.examples.account.http.stream.actor

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
import com.github.garyaiki.dendrites.examples.account.http.stream.ParallelCallFlow
import com.github.garyaiki.dendrites.stream.actor.{CallStream, OtherActor}
import ParallelCallSupervisor.SinkActor

/** Creates CallStream Actor with a RunnablaGraph composite flow of parallelCallFlow
  * logLeftRightFlow, and a Sink, which is an actor receiving the stream's results
  *
  * Watches Sink actor and changes state to waiting when it dies
  *
  * parallelCallsLogLeftPassRightFlow
  * {{{
  * bcast ~> check ~> zip.in0
  * bcast ~> mm ~> zip.in1 ~> logLeftRightFlow
  * bcast ~> savings ~> zip.in2
  * }}}
  *
  * @constructor initialized flow and Actor refs
  * @tparam A <: Product: TypeTag case class or tuple type passed to stream
  * @param initSinkActor: SinkActor case class with ActorRef, name
  * @author Gary Struthers
  */
class ParallelCallSupervisor[A <: Product: TypeTag](initSink: SinkActor) extends
        Actor with Stash with ActorLogging {

  implicit val system = context.system
  implicit val ec = system.dispatcher
  implicit val logger = log
  final implicit val mat: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))

  val pcf = new ParallelCallFlow()
  val wrappedFlow: Flow[A, Seq[AnyRef], NotUsed] = pcf.wrappedCallsLogLeftPassRightFlow
  val bufferSize = 10
  val overflowStrategy = OverflowStrategy.fail
  var sink: SinkActor = initSink
  val callStreamName = "CallStream" + typeOf[A].getClass.getSimpleName
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
    log.debug("preStart:{} callStream:{} sink:{}", this.toString(), callStreamName, sink.name)
    // create children here
    callStream = createCallStream(sink.ref)
  }

  override def preRestart(t: Throwable, msg: Option[Any]): Unit = {
    log.error(t, "preRestart cause [{}] on msg [{}]", t.getMessage, msg.getOrElse(""))
    context.unwatch(sink.ref)
    super.preRestart(t, msg) // stops children
  }

  /** SupervisorStrategy for child actors. Non-escalating errors are logged, these logs should guide
    * how to handle exceptions. case t invokes the default strategy for unnamed exceptions.
    * @see [[http://doc.akka.io/docs/akka/current/scala/fault-tolerance.html fault-tolerance]]
    */
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1.minute) {
      case _: EntityStreamSizeException => Resume
      case _: InvalidContentLengthException => Resume
      case _: MissingResourceException => Restart
      case _: RequestTimeoutException => Resume
      case _: EntityStreamException => Restart
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: IllegalHeaderException => Stop
      case _: IllegalRequestException => Stop
      case _: IllegalResponseException => Stop
      case _: IllegalStateException => Restart
      case _: IllegalUriException => Stop
      case _: ParsingException => Stop
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
    case Terminated(actor) ⇒ {
      context.parent ! SinkActor(null, sink.name)
      context.stop(callStream)
      context.unwatch(sink.ref)
      context.become(waiting)
      log.warning("sinkActor {} terminated", sink)
    }
    case x: A ⇒ callStream forward x
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
    case Terminated(actor) ⇒ {
      context.parent ! SinkActor(null, sink.name)
      context.unwatch(sink.ref)
      log.warning("sinkActor {} terminated while not ready", sink)
    }
    case newSink: SinkActor => {
      sink = newSink
      callStream = createCallStream(newSink.ref)
      unstashAll()
      context.become(ready)
    }
    case x: A ⇒ stash()
  }

  /** receive is ready for normal processing, waiting while updating ActorRef for Sink  */
  def receive = ready
}

object ParallelCallSupervisor {

  case class SinkActor(override val ref: ActorRef, override val name: String) extends OtherActor

  def props[A <: Product: TypeTag](sinkActor: SinkActor): Props =
    Props(new ParallelCallSupervisor[A](sinkActor))
}

