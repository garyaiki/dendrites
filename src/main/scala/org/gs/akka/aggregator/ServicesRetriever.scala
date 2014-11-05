package org.gs.akka.aggregator

import scala.collection._
import scala.concurrent.duration._
import scala.math.BigDecimal.int2bigDecimal
import scala.util.{ Success, Failure }
import scala.concurrent.TimeoutException

import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs.akka.aggregator.examples._
/**
 * Sample and test code for the aggregator patter.
 * This is based on Jamie Allen's tutorial at
 * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
 */

final case class TellServices[A](id: Long, proxyMessages: List[(Props, AnyRef)], resultType: A)

class ServicesRetriever[A] extends Actor with Aggregator {

  import context._

  expectOnce {
    case TellServices(id, proxies, resultType) ⇒
      new ServicesAggregator(sender(), id, proxies, resultType)
    case _ ⇒
      sender() ! CantUnderstand
      context.stop(self)
  }

  class ServicesAggregator[A](originalSender: ActorRef,
    id: Long, proxyMessages: List[(Props, AnyRef)], resultType: A) {

    val results = mutable.ArrayBuffer.empty[A]
    for (svcMsg <- proxyMessages) {
      context.actorOf(svcMsg._1) ! svcMsg._2
      expectOnce {
        case Success(a: A) ⇒
          results += a
          collectResults()
        case Failure(e) => reportFailure(e)
      }
    }

    context.system.scheduler.scheduleOnce(1.second, self, TimedOut)
    expect {
      case TimedOut ⇒ reportFailure(new TimeoutException("!!!"))
    }

    def reportFailure(e: Throwable) {
      originalSender ! new Failure(e)
      context.stop(self)
    }

    def collectResults() {
      if (results.size == proxyMessages.size) {
        originalSender ! results.toList // Make sure it becomes immutable

        context.stop(self)
      }
    }
  }
}