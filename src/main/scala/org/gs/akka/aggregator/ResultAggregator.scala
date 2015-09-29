/**
  */
package org.gs.akka.aggregator

import akka.actor.{ Actor, ActorRef, Props }
import akka.contrib.pattern.Aggregator
import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.Set
import scala.reflect.runtime.universe._
import akka.actor.ActorLogging
import org.gs.reflection._
/** @author garystruthers
  *
  */
trait ResultAggregator extends ActorLogging  with Aggregator {
  this: Actor ⇒

  private val results = new ArrayBuffer[Product]()

  def initResults[A <: Product](types: Set[A]): Unit = {
    results.appendAll(ArrayBuffer.fill[Product](types.size)(None))
  }
    
  def fetchResult(props: Props, pf:PartialFunction[Any, Unit], msg: Product, recipient: ActorRef) {
    log.debug(s"fetchResult props:$props pf:$pf, msg:$msg, recipient:${recipient.path}")
    this.context.actorOf(props) ! msg
    expectOnce(pf)
  }
  
  def addResult(i: Int, p: Product, recipient: ActorRef) = {
    results.update(i, p)
    collectResults(recipient)
  }

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