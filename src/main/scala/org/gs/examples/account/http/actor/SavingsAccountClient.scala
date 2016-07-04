package org.gs.examples.account.http.actor

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSystem, Props}
import akka.contrib.pattern.Aggregator
import akka.event.{LoggingAdapter, Logging}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import org.gs.aggregator.actor.ResultAggregator
import org.gs.examples.account.{Savings, SavingsAccountBalances, GetAccountBalances}
import org.gs.examples.account.http.{BalancesProtocols, SavingsBalancesClientConfig}
import org.gs.http.{caseClassToGetQuery, typedQuery, typedFutureResponse}

/**
  *
  * @author Gary Struthers
  */
class SavingsAccountClient(clientConfig: SavingsBalancesClientConfig) extends Actor with
  BalancesProtocols with ActorLogging {
  import context._
  override implicit val system = context.system
  override implicit val materializer = ActorMaterializer()
  implicit val logger = log
  val hostConfig = clientConfig.hostConfig
  val config = hostConfig._1
/*
  override def preStart() = {
    log.debug(s"Starting ${this.toString()}")
  }
*/
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse(""))
  }

  def receive = {
    case GetAccountBalances(id: Long) ⇒ {
      val cc = GetAccountBalances(id)
      val callFuture = typedQuery(clientConfig.baseURL, cc.productPrefix, caseClassToGetQuery)(cc)
      typedFutureResponse(mapPlain, mapSavings)(callFuture) pipeTo sender
    }
  }
}

object SavingsAccountClient {
  val clientConfig = new SavingsBalancesClientConfig()
  def props(clientConfig: SavingsBalancesClientConfig): Props =
    Props(new SavingsAccountClient(clientConfig))
}

trait SavingsAccountCaller {
  this: Actor with ResultAggregator with Aggregator ⇒

  val clientConfig = new SavingsBalancesClientConfig()

  def fetchSavingsAccountsBalance(context: ActorContext, id: Long, recipient: ActorRef): Unit = {
    context.actorOf(SavingsAccountClient.props(clientConfig)) ! GetAccountBalances(id)
    expectOnce {
      case SavingsAccountBalances(balances) ⇒
        addResult(0, (Savings -> balances), recipient)
    }
  }
}
