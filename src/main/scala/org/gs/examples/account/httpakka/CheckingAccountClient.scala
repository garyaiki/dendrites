package org.gs.examples.account.httpakka

import scala.concurrent.Future
import scala.util.Try
import com.typesafe.config.{ Config, ConfigFactory }
import akka.actor.{ Actor, ActorContext, ActorRef, ActorSystem, Props }
import akka.contrib.pattern.Aggregator
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import org.gs.akka.aggregator.ResultAggregator
import org.gs.akka.http.ClientConnectionPool
import org.gs.examples.account.{ Checking, CheckingAccountBalances, GetAccountBalances }
import org.gs.examples.account.http.BalancesProtocols
import org.gs.http._

import CheckingAccountClient._

class CheckingAccountClient extends Actor with BalancesProtocols {
  implicit val system = ActorSystem("akka-aggregator")
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val ip = config.getString("akka-aggregator.checking-balances.http.interface")
  val port = config.getInt("akka-aggregator.checking-balances.http.port")
  val path = config.getString("akka-aggregator.checking-balances.http.path")
  val checkingPath = createUrl("http", ip, port, path)
  
  private def call(id: Long): Future[HttpResponse] = {
    val balancesQuery = caseClassToGetQuery(GetAccountBalances(id))
    checkingPath.append(balancesQuery)
    Http().singleRequest(HttpRequest(uri = checkingPath.toString))
  }

  def receive = {
    case GetAccountBalances(id: Long) ⇒ {
      /*      val responseFuture = call(id)
      responseFuture onSuccess {
        case r => sender() ! r.entity 
      } */
      sender() ! CheckingAccountBalances(Some(List((3, 15000))))
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