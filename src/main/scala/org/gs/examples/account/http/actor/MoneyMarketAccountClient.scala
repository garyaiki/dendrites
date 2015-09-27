package org.gs.examples.account.http.actor

import akka.actor.{ Actor, ActorContext, ActorLogging, ActorRef, ActorSystem, Props }
import akka.contrib.pattern.Aggregator
import akka.event.{ LoggingAdapter, Logging }
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import com.typesafe.config.Config
import org.gs.akka.aggregator.ResultAggregator
import org.gs.examples.account.{ MoneyMarket, MoneyMarketAccountBalances, GetAccountBalances }
import org.gs.examples.account.http.{ BalancesClients, MoneyMarketBalancesClient }
import org.gs.http._


class MoneyMarketAccountClient extends Actor with BalancesClients with ActorLogging {
  import context._
  override implicit val system = context.system
  override implicit val materializer = ActorMaterializer()
  implicit val logger = log
  val client = new MoneyMarketBalancesClient()
  val hostConfig = client.hostConfig
  val config = hostConfig._1

  override def preStart() = {
    //log.debug(s"Starting ${this.toString()}")
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse(""))
  }

  def receive = {
    case GetAccountBalances(id: Long) ⇒ {
      val callFuture = HigherOrderCalls.call(GetAccountBalances(id), client.baseURL)
      val responseFuture = HigherOrderCalls.byId(id, callFuture, client.mapMoneyMarket, mapPlain)
      responseFuture pipeTo sender
    }
  }
}

object MoneyMarketAccountClient {
  def props = Props[MoneyMarketAccountClient]
}

trait MoneyMarketAccountCaller {
  this: Actor with ResultAggregator with Aggregator ⇒

  def fetchMoneyMarketAccountsBalance(context: ActorContext, id: Long, recipient: ActorRef) {
    context.actorOf(MoneyMarketAccountClient.props) ! GetAccountBalances(id)
    expectOnce {
      case MoneyMarketAccountBalances(balances) ⇒
        addResult(0, (MoneyMarket -> balances), recipient)
    }
  }
}