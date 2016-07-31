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
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.pipe
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, QueueOfferResult}
import akka.stream.OverflowStrategy
import akka.stream.OverflowStrategy.fail
import akka.stream.QueueOfferResult.Dropped
import akka.stream.QueueOfferResult.Enqueued
import akka.stream.QueueOfferResult.Failure
import akka.stream.QueueOfferResult.QueueClosed
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source, SourceQueueWithComplete}
import java.util.MissingResourceException
import scala.concurrent.Future
import scala.reflect.runtime.universe.TypeTag
import CallStream.CompleteMessage

/** Generic Actor that calls a stream in a RunnableGraph. All customization can be in the graph
  *
  * The RunnableGraph's Source must be created with Source.queue, which is materialized as a
  * SourceQueue. When receive get a message of type A it's offered to the stream. The offer returns
  * a Future of QueueOfferResult which is pipeTo offerResultHandler.
  * 
  * The graph's Sink can be created with Sink.actorRef. This will forward the sink's input to that
  * actorRef. Other types of Sink also work
  *
  * @tparam A case class or tuple passed to stream
  * @param rg complete RunnableGraph with Source and Sink. Not yet materialized 
  * @author Gary Struthers
  */
class CallStream[A: TypeTag](rg: RunnableGraph[SourceQueueWithComplete[A]]) extends Actor
        with ActorLogging {

  implicit val system = context.system
  import system.dispatcher

  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(system))

  var rgMaterialized: SourceQueueWithComplete[A] = null

  /** Materialize the RunnableGraph */
  override def preStart() = {
    log.debug("preStart {}", this.toString())
    rgMaterialized = rg.run
    super.preStart()
  }

  /** Complete the stream */
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Restarting due to {} when processing {}",
        reason.getMessage, message.getOrElse(""))
    rgMaterialized.complete()
    super.preRestart(reason, message)
  }

  /** Handle QueueOfferResult. SourceQueue.offer returns Future[QueueOfferResult]. Because Actors
    * should not have future callbacks, pipeTo self sends the future back to this actor, it's
    * completed and receive gets QueueOfferResult
    *
    * @param offerResult
    */
  def offerResultHandler(offerResult: QueueOfferResult): Unit = {
    offerResult match {
      case Dropped => log.warning("Stream dropped element")
      case Enqueued => log.debug("Stream enqueued element")
      case f:Failure => {
        log.error(f.cause, "Stream enqueue failed")
        throw(f.cause)
      }
      case QueueClosed => {
        val msg = "Stream completed before enqueue"
        val e = new MissingResourceException(msg, rg.getClass.getName, "")
        log.error(e, msg)
        throw(e)
      }
      case x => log.warning("unknown offerResult {}", x)
    }
  }

  /** Generic receive for RunnableGraphs beginning with a SourceQueue
    * message type A is offered to the SourceQueue. The offer result is returned as a
    * Future[QueueOfferResult] and pipeTo this actor. If this message is successfully enqueued, the
    * stream processes it
    * 
    * message type QueueOfferResult is passed to offerResultHanlder which logs all types and throws
    * an exception for errors
    * 
    * message everythingElse is the fall through and is logged
    */
  def receive = {

    case CompleteMessage => {
      log.debug("CompleteMessage from {}", sender)
    }
    case y: QueueOfferResult => {
      offerResultHandler(y)
    }

    case x: A ⇒ {
      log.debug("Type A msg {}", x)
      val offerFuture: Future[QueueOfferResult] = rgMaterialized.offer(x)
      offerFuture pipeTo self
    }

    case everythingElse => {
      log.warning("unknown message {}", everythingElse)
    }
  }
}

/** Actor messages and Props factory */
object CallStream {

  /** message sent to ActorRef when stream completes */
  case object CompleteMessage

  /** Create CallStream Props for stream that don't send messages from their Sink
    *
    * @param flow Akka stream Flow
    * @return Props to create actor
  */
  def props[A: TypeTag](runnable: RunnableGraph[SourceQueueWithComplete[A]]): Props = {
    Props(new CallStream[A](runnable))
  }

  /** Create CallStream Props for stream that sends Sink input to the Actor Ref
    *
    * @param sinkRef ActorRef that becomes the Sink
    * @param flow Akka stream Flow
    * @return Props to create actor
  	*/
  def props[A: TypeTag](sinkRef: ActorRef,
          flow: Flow[A, (Seq[String], Seq[AnyRef]),
          NotUsed]): Props = {
    val source = Source.queue[A](10, OverflowStrategy.fail)
    val sink = Sink.actorRef(sinkRef, CompleteMessage)
    val runnable: RunnableGraph[SourceQueueWithComplete[A]] = source.via(flow).to(sink)
    Props(new CallStream[A](runnable))
  }
}
