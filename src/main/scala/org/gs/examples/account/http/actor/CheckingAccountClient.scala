package org.gs.examples.account.http.actor

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSystem, Props}
import akka.contrib.pattern.Aggregator
import akka.event.{LoggingAdapter, Logging}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.Config
import org.gs.aggregator.actor.ResultAggregator
import org.gs.examples.account.{Checking, CheckingAccountBalances, GetAccountBalances}
import org.gs.examples.account.http.{BalancesProtocols, CheckingBalancesClientConfig}
import org.gs.http.{caseClassToGetQuery, typedQuery, typedFutureResponse}

/**
  *
  * @author Gary Struthers
  */
class CheckingAccountClient(clientConfig: CheckingBalancesClientConfig) extends Actor with
  BalancesProtocols with ActorLogging {
  import context._
  override implicit val system = context.system
  override implicit val materializer = ActorMaterializer()
  implicit val logger = log
  val hostConfig = clientConfig.hostConfig
  val config = hostConfig._1
  implicit val timeout: Timeout = clientConfig.timeout
  /** for debugging
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
      typedFutureResponse(mapPlain, mapChecking)(callFuture) pipeTo sender
    }
  }
}

object CheckingAccountClient {
  val clientConfig = new CheckingBalancesClientConfig()
  def props(clientConfig: CheckingBalancesClientConfig): Props =
    Props(new CheckingAccountClient(clientConfig))
}

trait CheckingAccountCaller {
  this: Actor with ResultAggregator with Aggregator ⇒

  val clientConfig = CheckingAccountClient.clientConfig

  def fetchCheckingAccountsBalance(context: ActorContext, id: Long, recipient: ActorRef): Unit = {
    context.actorOf(CheckingAccountClient.props(clientConfig)) ! GetAccountBalances(id)
    expectOnce {
      case CheckingAccountBalances(balances) ⇒
        addResult(0, (Checking -> balances), recipient)
    }
  }
}
