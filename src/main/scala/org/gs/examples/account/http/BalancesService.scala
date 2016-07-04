package org.gs.examples.account.http

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import spray.json.DefaultJsonProtocol
import org.gs.examples.account.{CheckingAccountBalances, GetAccountBalances,
    MoneyMarketAccountBalances, SavingsAccountBalances}

/** Map json <=> case classes to Either Right on success, String to Left on failure
  *
  * @author Gary Struthers
  *
  */
trait BalancesProtocols extends DefaultJsonProtocol {
  implicit val getAccountBalancesFormat = jsonFormat1(GetAccountBalances)
  implicit val checkingAccountBalancesFormat = jsonFormat1(CheckingAccountBalances[BigDecimal])
  implicit val moneyMarketAccountBalancesFormat = jsonFormat1(MoneyMarketAccountBalances[BigDecimal])
  implicit val savingsAccountBalancesFormat = jsonFormat1(SavingsAccountBalances[BigDecimal])

  implicit val system: ActorSystem
  implicit val materializer: Materializer

  def mapChecking(entity: HttpEntity): Future[Right[String, AnyRef]] = {
    Unmarshal(entity).to[CheckingAccountBalances[BigDecimal]].map(Right(_))
  }

  def mapPlain(entity: HttpEntity): Future[Left[String, Nothing]] = {
    Unmarshal(entity).to[String].map(Left(_))
  }

  def mapMoneyMarket(entity: HttpEntity): Future[Right[String, AnyRef]] = {
    Unmarshal(entity).to[MoneyMarketAccountBalances[BigDecimal]].map(Right(_))
  }

  def mapSavings(entity: HttpEntity): Future[Right[String, AnyRef]] = {
    Unmarshal(entity).to[SavingsAccountBalances[BigDecimal]].map(Right(_))
  }
}

/** Example Rest/microserver using Akka Http DSL. Valid routes call accessors for dummy data
  *
  */ 
trait BalancesService extends BalancesProtocols {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def fetchCheckingBalances(id: Long): Either[String, CheckingAccountBalances[BigDecimal]] = {
    checkingBalances.get(id) match {
      case Some(x) => Right(CheckingAccountBalances(x))
      case None    => Left(s"Checking account $id not found")
      case _       => Left(s"Error looking up checking account $id")
    }
  }

  def fetchMMBalances(id: Long): Either[String, MoneyMarketAccountBalances[BigDecimal]] = {
    moneyMarketBalances.get(id) match {
      case Some(x) => Right(MoneyMarketAccountBalances(x))
      case None    => Left(s"Money Market account $id not found")
      case _       => Left(s"Error looking up Money Market account $id")
    }
  }

  def fetchSavingsBalances(id: Long): Either[String, SavingsAccountBalances[BigDecimal]] = {
    savingsBalances.get(id) match {
      case Some(x) => Right(SavingsAccountBalances(x))
      case None    => Left(s"Savings account $id not found")
      case _       => Left(s"Error looking up Savings account $id")
    }
  }

  val routes = {
    logRequestResult("org.gs.examples.account.http.balancesServer") {

      path("account" / "balances" / "checking" / "GetAccountBalances") {
        parameter('id.as[Long]) { id =>
          complete {
            fetchCheckingBalances(id)
          }
        }
      } ~
        path("account" / "balances" / "mm" / "GetAccountBalances") {
          parameter('id.as[Long]) { id =>
            complete {
              fetchMMBalances(id)
            }
          }
        } ~
        path("account" / "balances" / "savings" / "GetAccountBalances") {
          parameter('id.as[Long]) { id =>
            complete {
              fetchSavingsBalances(id)
            }
          }
        }
    }
  }
}
