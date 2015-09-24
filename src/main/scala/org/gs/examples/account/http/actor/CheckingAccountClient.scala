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
import org.gs.examples.account.{ Checking, CheckingAccountBalances, GetAccountBalances }
import org.gs.examples.account.http.{ BalancesClients, CheckingBalancesClient }

import CheckingAccountClient._

class CheckingAccountClient extends Actor with BalancesClients with ActorLogging {
  import context._

  override def preStart() = {
    //log.debug(s"Starting ${this.toString()}")
  }
  
  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
        reason.getMessage, message.getOrElse(""))
  }
  
  import CheckingBalancesClient._
  val hostConfig = CheckingBalancesClient.getHostConfig()
  val config = hostConfig._1
  override implicit val system = context.system
  override implicit val materializer = ActorMaterializer()
  override val logger = log
  def receive = {
    case GetAccountBalances(id: Long) ⇒ {
      try {
      val f = requestCheckingBalances(id, CheckingBalancesClient.configBaseUrl(hostConfig))
      f pipeTo sender
      } catch {
        case e: Exception =>
          sender() ! akka.actor.Status.Failure(e)
      }
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