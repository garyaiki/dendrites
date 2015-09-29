package org.gs.examples.account.akka

import akka.actor.{Actor, ActorContext, ActorLogging }
import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs.akka.aggregator.ResultAggregator
import org.gs.examples.account.{Checking, CheckingAccountBalances, GetAccountBalances}
import CheckingAccountProxy._

class CheckingAccountProxy extends Actor with ActorLogging {
  
  override def preStart() = {
    //log.debug(s"Starting ${this.toString()}")
  }
  
  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
        reason.getMessage, message.getOrElse(""))
  }
  
  def receive = {
    case GetAccountBalances(id: Long) ⇒
      sender() ! CheckingAccountBalances(Some(List((3, 15000))))
  }
}

object CheckingAccountProxy {
  def props = Props[CheckingAccountProxy]
}

trait CheckingAccountFetcher {
  this: Actor with ResultAggregator with Aggregator ⇒

  
  def fetchCheckingAccountsBalance(props: Props, context: ActorContext, id: Long, recipient: ActorRef) {
    //val props = CheckingAccountProxy.props
    printf("CheckingAccountProxy Props: %s\n", props)
    context.actorOf(props) ! GetAccountBalances(id)
    expectOnce {
      case CheckingAccountBalances(balances) ⇒
        addResult(0, (Checking -> balances), recipient)
    }
  }
  
  def fetchAccountsBalance(props: Props, pf:PartialFunction[Any, Unit], msg: Product, recipient: ActorRef) {
/*    def pf:PartialFunction[Any, Unit] = {
      case CheckingAccountBalances(balances) ⇒
        addResult(0, (Checking -> balances), recipient)
    }*/
    //val props = CheckingAccountProxy.props
    print(s"CheckingAccountProxy Props:$props PartialFunction:$pf")
    this.context.actorOf(props) ! msg
    expectOnce(pf) /*{
      case CheckingAccountBalances(balances) ⇒
        addResult(0, (Checking -> balances), recipient)
    }*/
  }
}