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
package com.github.garyaiki.dendrites.stream.actor

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status}
import akka.actor.Status.{Failure => StatusFailure}
import akka.pattern.pipe
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, QueueOfferResult}
import akka.stream.OverflowStrategy
import akka.stream.OverflowStrategy.fail
import akka.stream.QueueOfferResult.{Dropped, Enqueued, Failure, QueueClosed}
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source, SourceQueueWithComplete}
import java.util.MissingResourceException
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag
import CallStream.CompleteMessage

/** Generic Actor that calls a stream in a RunnableGraph. All customization can be in the graph
  *
  * The RunnableGraph's Source must be created with Source.queue, which is materialized as a
  * SourceQueue. When receive get a message of type A it's offered to the stream. The offer returns
  * a Future of QueueOfferResult which is pipeTo offerResultHandler.
  *
  * An exception in the RunnableGraph that isn't handled by its own Supervision will stop the stream
  * and a Failure message will be received. The original NonFatal exception isn't thrown to the
  * actor. Instead the Actor throws an IllegalStateException and its Supervisor should restart the
  * actor (and its stream). Too many retries should cause the Supervisor to Stop the actor.
  *
  * The graph's Sink can be created with Sink.actorRef. This will forward the sink's input to that
  * actorRef. Other types of Sink also work
  *
  * @tparam A: TypeTag case class or tuple passed to stream
  * @param rg complete RunnableGraph with Source and Sink. Not yet materialized
  * @see [[http://doc.akka.io/api/akka/2.4/#akka.stream.javadsl.SourceQueueWithComplete SourceQueueWithComplete]]
  * @author Gary Struthers
  */
class CallStream[A: TypeTag](rg: RunnableGraph[SourceQueueWithComplete[A]]) extends Actor with ActorLogging {

  implicit val system = context.system
  import system.dispatcher
  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))

  var rgMaterialized: SourceQueueWithComplete[A] = null

  /** Materialize the RunnableGraph */
  override def preStart() = {
    log.debug("preStart:{}", this.toString)
    rgMaterialized = rg.run
  }

  /** Complete the stream */
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "preRestart due to {} when processing {}", reason.getMessage, message.getOrElse(""))
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
      case Dropped => log.warning("offerResult Dropped")
      case Enqueued => log.debug("offerResult Enqueued")
      case f:Failure => {
        log.error(f.cause, "offerResult Failure {}", f.cause.getMessage)
        throw(f.cause)
      }
      case QueueClosed => {
        val msg = "QueueClosed: stream completed before enqueue"
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

    case CompleteMessage => log.debug("receive CompleteMessage from {}", sender)

    case y: QueueOfferResult => offerResultHandler(y)

    case s: StatusFailure => {
      val t = s.cause
      log.error(t, "receive StatusFailure: enqueue:{} ex:{}", t.getMessage, t.getClass.getName)
      throw(t)
    }

    case m: A => {
      log.debug("receive msg:{} class:{}", m, m.getClass.getName)
      val offerFuture: Future[QueueOfferResult] = rgMaterialized.offer(m)
      offerFuture pipeTo self
    }

    case everythingElse => log.warning("receive unknown message {}", everythingElse)
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
  def props[A: TypeTag](rg: RunnableGraph[SourceQueueWithComplete[A]]): Props = Props(new CallStream[A](rg))

  def props[A: TypeTag](sinkRef: ActorRef, flow: Flow[A, Seq[AnyRef], NotUsed]): Props = {
    val source = Source.queue[A](1, OverflowStrategy.fail)
    val sink = Sink.actorRef(sinkRef, CompleteMessage)
    val rg: RunnableGraph[SourceQueueWithComplete[A]] = source.via(flow).to(sink)
    Props(new CallStream[A](rg))
  }
}
