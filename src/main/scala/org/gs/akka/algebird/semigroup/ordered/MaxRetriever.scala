package org.gs.akka.algebird.semigroup.ordered

import scala.collection._
import scala.concurrent.duration._
import scala.math.BigDecimal.int2bigDecimal
 
import akka.actor._
import akka.contrib.pattern.Aggregator
import com.twitter.algebird.Operators._
import com.twitter.algebird.Max
import com.twitter.algebird.Max._
import org.gs.akka.aggregator.{CantUnderstand, TimedOut}
import org.gs.akka.aggregator.{CheckingAccountProxy, CheckingAccountBalances, Checking}
import org.gs.akka.aggregator.{SavingsAccountProxy, SavingsAccountBalances, Savings}
/**
 * Sample and test code for the aggregator patter.
 * This is based on Jamie Allen's tutorial at
 * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
 */
 
final case class GetCustomerAccountBalances[A](id: Long, balanceType: A)
final case class GetNumberResult(id: Long)
class FirstServiceProxy extends Actor {
  def receive = {
    case GetNumberResult(id: Long) ⇒
      sender() ! Some(15000)
  }
}

class SecondServiceProxy extends Actor {
  def receive = {
    case GetNumberResult(id: Long) ⇒
      sender() ! Some(5000)
  }
}

 
class MaxRetriever[A] extends Actor with Aggregator {
 
  import context._
 
  expectOnce {
    case GetCustomerAccountBalances(id, balanceType) ⇒
      new MaxAggregator(sender(), id, balanceType)
    case _ ⇒
      sender() ! CantUnderstand
      context.stop(self)
  }
 
  class MaxAggregator[A](originalSender: ActorRef,
                          id: Long, balanceType: A) {
 
    val results = mutable.ArrayBuffer.empty[A]
 
    fetchCheckingAccountsBalance()
    fetchSavingsAccountsBalance()
 
    context.system.scheduler.scheduleOnce(1.second, self, TimedOut)
    expect {
      case TimedOut ⇒ collectBalances(force = true)
    }
 
    def fetchCheckingAccountsBalance() {
      context.actorOf(Props[FirstServiceProxy]) ! GetNumberResult(id)
      expectOnce {
        case Some(a:A) ⇒
          results += a
          collectBalances()
        case None => collectBalances(true)
      }
    }
 
    def fetchSavingsAccountsBalance() {
      context.actorOf(Props[SecondServiceProxy]) ! GetNumberResult(id)
      expectOnce {
        case Some(a: A) ⇒
          results += a
          collectBalances()
        case None => collectBalances(true)
      }
    }
 
    def collectBalances(force: Boolean = false) {
      if (results.size == 2 || force) {
        originalSender ! results.toList // Make sure it becomes immutable
        context.stop(self)
      }
    }
  }
}