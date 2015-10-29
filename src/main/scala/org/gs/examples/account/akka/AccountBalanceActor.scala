package org.gs.examples.account.akka

import akka.actor._
import org.gs.akka.aggregator.{ CantUnderstand, ResultAggregator, TimedOut }
import org.gs.examples.account._
import scala.collection.immutable.Set
import scala.concurrent.duration._
import org.gs.akka.aggregator.PartialFunctionPlusSender

/** Actor code common to AccountBalanceRetriever and AccountBalanceRestfulRetriever.
 *  
  * This is based on Jamie Allen's tutorial at
  * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
  */
trait AccountBalanceActor extends Actor
  with ResultAggregator
  with PartialFunctionPlusSender {

  import context._

  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse(""))
  }

  class AccountAggregator(originalSender: ActorRef, id: Long, actors: Map[AccountType, Props]) {
    def f = pfPlusSender(originalSender)(_)
    val pf = PartialFunction(f)
    val msg = new GetAccountBalances(id)
    val types = actors.keySet
    initResults[AccountType](types)
    if (types.size > 0)
      types foreach {
        case Checking    ⇒ fetchResult(actors.get(Checking).get, pf, msg, originalSender)
        case Savings     ⇒ fetchResult(actors.get(Savings).get, pf, msg, originalSender)
        case MoneyMarket ⇒ fetchResult(actors.get(MoneyMarket).get, pf, msg, originalSender)
      }
    else collectResults(originalSender)

    system.scheduler.scheduleOnce(3.second, self, TimedOut)
    expect {
      case TimedOut ⇒ collectResults(originalSender, force = true)
    }
  }
}

