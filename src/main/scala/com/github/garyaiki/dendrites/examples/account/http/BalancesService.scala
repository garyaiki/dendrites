/** Copyright 2016 Gary Struthers

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.garyaiki.dendrites.examples.account.http

import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.{sprayJsonMarshaller, sprayJsonUnmarshaller}
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import spray.json.DefaultJsonProtocol
import com.github.garyaiki.dendrites.examples.account.{CheckingAccountBalances,
  GetAccountBalances,
  MoneyMarketAccountBalances,
  SavingsAccountBalances}

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

  implicit val mat: Materializer

  /** Unmarshall HttpEntity result
    * @param entity
    * @return Future case class
    * @see [[http://doc.akka.io/api/akka/current/#akka.http.scaladsl.unmarshalling.Unmarshaller$$NoContentException$ NoContentException]]
    * @throws NoContentException
    */
  def mapChecking(entity: HttpEntity): Future[Right[String, AnyRef]] = {
    Unmarshal(entity).to[CheckingAccountBalances[BigDecimal]].map(Right(_))
  }

  /** Unmarshall HttpEntity error
    * @param entity
    * @return Future error message
    * @see [[http://doc.akka.io/api/akka/current/#akka.http.scaladsl.unmarshalling.Unmarshaller$$NoContentException$ NoContentException]]
    * @throws NoContentException
    */
  def mapPlain(entity: HttpEntity): Future[Left[String, Nothing]] = Unmarshal(entity).to[String].map(Left(_))

  /** Unmarshall HttpEntity result
    * @param entity
    * @return Future case class
    * @see [[http://doc.akka.io/api/akka/current/#akka.http.scaladsl.unmarshalling.Unmarshaller$$NoContentException$ NoContentException]]
    * @throws NoContentException
    */
  def mapMoneyMarket(entity: HttpEntity): Future[Right[String, AnyRef]] = {
    Unmarshal(entity).to[MoneyMarketAccountBalances[BigDecimal]].map(Right(_))
  }

  /** Unmarshall HttpEntity result
    * @param entity
    * @return Future case class
    * @see [[http://doc.akka.io/api/akka/current/#akka.http.scaladsl.unmarshalling.Unmarshaller$$NoContentException$ NoContentException]]
    * @throws NoContentException
    */
  def mapSavings(entity: HttpEntity): Future[Right[String, AnyRef]] = {
    Unmarshal(entity).to[SavingsAccountBalances[BigDecimal]].map(Right(_))
  }
}

/** Example Rest/microserver using Akka Http DSL. Valid routes call accessors for dummy data
  *
  */
trait BalancesService extends BalancesProtocols {
  implicit def executor: ExecutionContextExecutor
  implicit val mat: Materializer

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
    logRequestResult("com.github.garyaiki.dendrites.examples.account.http.balancesServer") {

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
