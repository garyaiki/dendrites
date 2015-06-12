package org.gs.examples.account.akkahttp

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorFlowMaterializer, FlowMaterializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.IOException
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.math._
import spray.json.DefaultJsonProtocol

import org.gs.examples.account._
import org.gs.examples.account.akkahttp._


trait BalancesProtocols extends DefaultJsonProtocol {
  implicit val getAccountBalancesFormat = jsonFormat1(GetAccountBalances)
  implicit val checkingAccountBalancesFormat = jsonFormat1(CheckingAccountBalances)
  implicit val moneyMarketAccountBalancesFormat = jsonFormat1(MoneyMarketAccountBalances)
  implicit val savingsAccountBalancesFormat = jsonFormat1(SavingsAccountBalances)
}

trait BalancesService extends BalancesProtocols {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: FlowMaterializer

  def config: Config
  val logger: LoggingAdapter

  def fetchCheckingBalances(id: Long): Either[String, CheckingAccountBalances] = {
    checkingBalances.get(id) match {
      case Some(x) => Right(CheckingAccountBalances(x))
      case None => Left(s"Checking account $id not found")
      case _ => Left(s"Error looking up checking account $id")
    }
  }

  def fetchMMBalances(id: Long): Either[String, MoneyMarketAccountBalances] = {
    moneyMarketBalances.get(id) match {
      case Some(x) => Right(MoneyMarketAccountBalances(x))
      case None => Left(s"Money Market account $id not found")
      case _ => Left(s"Error looking up Money Market account $id")
    }
  }

  
  def fetchSavingsBalances(id: Long): Either[String, SavingsAccountBalances] = {
    savingsBalances.get(id) match {
      case Some(x) => Right(SavingsAccountBalances(x))
      case None => Left(s"Savings account $id not found")
      case _ => Left(s"Error looking up Savings account $id")
    }
  }
  
  val routes = {
    logRequestResult("org.gs.examples.account.akkahttp.microserver") {
      pathSingleSlash {
        complete("root")
      } ~
        path("account" / "balances" / "checking") {
          (post & entity(as[GetAccountBalances])) { getAccountBalances =>
            complete {
              fetchCheckingBalances(getAccountBalances.id)
            }
          }
        } ~
        path("account" / "balances" / "mm") {
          (post & entity(as[GetAccountBalances])) { getAccountBalances =>
            complete {
              fetchMMBalances(getAccountBalances.id)
            }
          }
        } ~
        path("account" / "balances" / "savings") {
          (post & entity(as[GetAccountBalances])) { getAccountBalances =>
            complete {
              fetchSavingsBalances(getAccountBalances.id)
            }
          }
        }
    }
  }
}

object Microserver extends App with BalancesService {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
