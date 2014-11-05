/**
 *
 */
package org.gs.akka.aggregator

import akka.actor._
import akka.actor.ActorContext
import akka.contrib.pattern.Aggregator
import scala.util.{ Try, Success, Failure }

/**
 * @author garystruthers
 *
 */
package object examples {

  val exampleId = 1
  val exampleResult1 = 15000L
  val exampleResult2 = 5000L
  val exampleError = new NoSuchElementException("force failure for test")

  final case class GetServiceResult(id: Long)

  object FirstServiceProxy {
    def props(): Props = Props(new FirstServiceProxy())
  }

  class FirstServiceProxy extends Actor {
    def receive = {
      case GetServiceResult(id: Long) ⇒
        sender() ! Success(exampleResult1)
    }
  }

  object SecondServiceProxy {
    def props(): Props = Props(new SecondServiceProxy())
  }

  class SecondServiceProxy extends Actor {
    def receive = {
      case GetServiceResult(id: Long) ⇒
        sender() ! Success(exampleResult2)
    }
  }

  object BadServiceProxy {
    def props(): Props = Props(new BadServiceProxy())
  }
  
  class BadServiceProxy extends Actor {
    def receive = {
      case GetServiceResult(id: Long) ⇒
        sender() ! Failure(exampleError)
    }
  }
  val proxies = List((Props[FirstServiceProxy], GetServiceResult(exampleId)),
    (Props[SecondServiceProxy], GetServiceResult(exampleId)))

  val badProxies = List((Props[FirstServiceProxy], GetServiceResult(exampleId)),
    (Props[BadServiceProxy], GetServiceResult(exampleId)))
}