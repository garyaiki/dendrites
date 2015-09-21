package org.gs.examples.account.httpakka


import akka.actor.{ Actor, ActorContext, ActorRef, ActorSystem, Props }
import akka.contrib.pattern.Aggregator
import akka.event.{ LoggingAdapter, Logging }
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import com.typesafe.config.Config
import org.gs.akka.aggregator.ResultAggregator
import org.gs.examples.account.{ Checking, CheckingAccountBalances, GetAccountBalances }
import org.gs.examples.account.http.{ BalancesClients, CheckingBalancesClient }

import CheckingAccountClient._

class CheckingAccountClient extends Actor with BalancesClients {
  import context._
  import CheckingBalancesClient._
  val hostConfig = CheckingBalancesClient.getHostConfig()
  val config = hostConfig._1
  override implicit val system = context.system
  override implicit val materializer = ActorMaterializer()
  override val logger = Logging(system, getClass)
  def receive = {
    case GetAccountBalances(id: Long) ⇒ {
      val f = requestCheckingBalances(id, CheckingBalancesClient.configBaseUrl(hostConfig))
      f pipeTo sender
    }
  }
}

object CheckingAccountClient {
  def props = Props[CheckingAccountClient]
}

trait CheckingAccountCaller {
  this: Actor with ResultAggregator with Aggregator ⇒

  def fetchCheckingAccountsBalance(context: ActorContext, id: Long, recipient: ActorRef) {
    context.actorOf(CheckingAccountClient.props) ! GetAccountBalances(id)
    expectOnce {
      case CheckingAccountBalances(balances) ⇒
        addResult(0, (Checking -> balances), recipient)
    }
  }
}