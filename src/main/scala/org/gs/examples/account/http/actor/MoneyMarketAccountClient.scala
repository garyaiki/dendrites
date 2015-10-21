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
import org.gs.examples.account.http.{ BalancesProtocols, MoneyMarketBalancesClientConfig }
import org.gs.http._

class MoneyMarketAccountClient(clientConfig: MoneyMarketBalancesClientConfig) extends Actor with
        BalancesProtocols with ActorLogging {
  import context._
  override implicit val system = context.system
  override implicit val materializer = ActorMaterializer()
  implicit val logger = log
  val hostConfig = clientConfig.hostConfig
  val config = hostConfig._1
  /* for debugging
  override def preStart() = {
    log.debug(s"Starting ${this.toString()}")
  }
*/
  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse(""))
  }

  def receive = {
    case GetAccountBalances(id: Long) ⇒ {
      val callFuture = typedQuery(GetAccountBalances(id), clientConfig.baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapMoneyMarket)
      responseFuture pipeTo sender
    }
  }
}

object MoneyMarketAccountClient {
  val clientConfig = new MoneyMarketBalancesClientConfig()
  def props(clientConfig: MoneyMarketBalancesClientConfig): Props = Props(new MoneyMarketAccountClient(clientConfig))
}

trait MoneyMarketAccountCaller {
  this: Actor with ResultAggregator with Aggregator ⇒

  val clientConfig = new MoneyMarketBalancesClientConfig()

  def fetchMoneyMarketAccountsBalance(context: ActorContext, id: Long, recipient: ActorRef) {
    context.actorOf(MoneyMarketAccountClient.props(clientConfig)) ! GetAccountBalances(id)
    expectOnce {
      case MoneyMarketAccountBalances(balances) ⇒
        addResult(0, (MoneyMarket -> balances), recipient)
    }
  }
}