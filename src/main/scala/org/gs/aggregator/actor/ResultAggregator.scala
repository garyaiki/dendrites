/**
  */
package org.gs.aggregator.actor

import akka.actor.{ Actor, ActorRef, Props }
import akka.contrib.pattern.Aggregator
import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.Set
import scala.reflect.runtime.universe._
import akka.actor.ActorLogging
import org.gs.reflection._
/** Pull up Aggregator functions to a higher level
  * 
  * @author garystruthers
  *
  */
trait ResultAggregator extends ActorLogging with Aggregator {
  this: Actor ⇒

  /** accumulater of values returned by actors */
  private val results = new ArrayBuffer[Product]()

  /** init results, None is used as an empty valued case class
    * @param types used to size buffer
    */
  def initResults[A <: Product](types: Set[A]): Unit = {
    results.appendAll(ArrayBuffer.fill[Product](types.size)(None))
  }

  /** Create child actor, send request handle response 
    *
    * @param props to create the actor
    * @param pf PartialFunction used by expectOnce to handle response
    * @param msg request message to created actor
    * @param recipient original sender of request to send response to
    */
  def fetchResult(props: Props, pf: PartialFunction[Any, Unit], msg: Product, recipient: ActorRef) {
    log.debug(s"fetchResult props:$props pf:$pf, msg:$msg, recipient:${recipient.path}")
    this.context.actorOf(props) ! msg
    expectOnce(pf)
  }

  /** Put child actor response in results, if it's the last one, send results to original sender
    *
    * @param i result index for this response
    * @param p child actor's response
    * @param recipient original sender
    */
  def addResult(i: Int, p: Product, recipient: ActorRef) = {
    results.update(i, p)
    collectResults(recipient)
  }

  /** If all responses are received send them to the original and stop parent and child actors 
    * 
    * @param recipient original sender
    * @param force if true send incomplete results
    */
  def collectResults(recipient: ActorRef, force: Boolean = false) {
    val resultCount = results.count(_ != None)
    if ((resultCount == results.size) || force) {
      val result = results.toIndexedSeq // Make sure it's immutable
      log.debug(s"$result:${weakParamInfo(result)} cnt:$resultCount types size:${results.size} frc:$force")
      recipient ! result
      context.stop(self)
    }
  }

}