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
import akka.actor.{Actor, ActorLogging, ActorRef, Stash, Terminated}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, OverflowStrategy}
import akka.stream.scaladsl.Source
import scala.concurrent.duration._
import scala.reflect.runtime.universe._
import StreamSinkRefSupervisor.SinkActor

/** Abstract supervisor for supervisors of Actors that call an Akka Stream composed of a
  * SourceQueue with parameterized type, Flow, and a Sink that's an actorRef
  *
  * @see [[http://doc.akka.io/docs/akka/current/general/supervision.html supervision]]
  * @see [[http://doc.akka.io/api/akka/current/#akka.actor.Stash Stash]]
  * @author Gary Struthers
  *
  * @tparam A case class or tuple type passed to stream
  */
abstract class StreamSinkRefSupervisor[A <: Product: TypeTag] extends
        Actor with Stash with ActorLogging {

  implicit val system = context.system
  implicit val logger = log
  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(system))

  val bufferSize: Int
  val overflowStrategy: OverflowStrategy
  val source = Source.queue[A](bufferSize, overflowStrategy)
  val onCompleteMessage = "completed"
  var sinkActor: SinkActor
  val callStreamName = "CallStream " + typeOf[A].getClass.getSimpleName
  var callStream: ActorRef = null

  /** Create a CallStream Actor. First watch the ActorRef of the Sink's target.
    *
  	* @param sinkRef ActorRef passed to Sink
  	* @return CallStream ActorRef
  	*/
  def createCallStream(ref: ActorRef): ActorRef

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

/** Actor messages and Props factory */
object StreamSinkRefSupervisor {
  case class SinkActor(override val ref: ActorRef, override val name: String) extends OtherActor  
}
