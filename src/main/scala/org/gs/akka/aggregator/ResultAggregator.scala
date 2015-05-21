/**
  */
package org.gs.akka.aggregator

import akka.actor.{ Actor, ActorRef }
import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.Set
import scala.reflect.runtime.universe._
import akka.actor.ActorLogging
import org.gs.reflection._
/** @author garystruthers
  *
  */
trait ResultAggregator extends ActorLogging {
  this: Actor ⇒

  private val results = new ArrayBuffer[Product]()

  def initResults[A <: Product](types: Set[A]): Unit = {
    results.appendAll(ArrayBuffer.fill[Product](types.size)(None))
  }
  
  def addResult(i: Int, p: Product, recipient: ActorRef) = {
    results.update(i, p)
    collectResults(recipient)
  }

  def collectResults(recipient: ActorRef, force: Boolean = false) {
    val resultCount = results.count(_ != None)
    if ((resultCount == results.size) || force) {
      val result = results.toIndexedSeq
      log.debug(s"$result:${weakParamInfo(result)} cnt:$resultCount types size:${results.size} frc:$force")
      recipient ! result // Make sure it's immutable
      context.stop(self)
    }
  }

}