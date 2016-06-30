package org.gs.examples.account.http

import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest._
import scala.math.BigDecimal.double2bigDecimal
import org.gs.examples.account.{CheckingAccountBalances, GetAccountBalances,
  MoneyMarketAccountBalances, SavingsAccountBalances}
import org.gs.http.caseClassToGetQuery

/**
  *
  * @author Gary Struthers
  */
class BalancesServiceSpec extends FlatSpec
        with Matchers with ScalatestRouteTest with BalancesService {
  override def testConfigSource = "akka.loglevel = WARNING"
  def config = testConfig
  val logger = NoLogging

  val goodId = 1L
  val balanceQuery = GetAccountBalances(goodId)
  val badId = 4L
  val badBalanceQuery = GetAccountBalances(badId)

  val checkingBalances = CheckingAccountBalances[BigDecimal](Some(List((goodId, 1000.1))))

  val checkingPath = "/account/balances/checking/"
  val balancesQuery = caseClassToGetQuery(balanceQuery, balanceQuery.productPrefix)
  val badBalancesQuery = caseClassToGetQuery(badBalanceQuery, badBalanceQuery.productPrefix)
  val q = checkingPath ++ balancesQuery
  val badQ = checkingPath ++ badBalancesQuery
  println(q)

  "BalancesService" should "respond handled = false for single slash query" in {
    Get(s"/") ~> routes ~> check {
      handled shouldEqual false
    }
  }

  it should "return existing Checking Account Balances" in {
    Get(q) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[CheckingAccountBalances[BigDecimal]] shouldBe checkingBalances
    }
  }

  it should "return Error message for non-existing Checking Account Balances" in {
    Get(badQ) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `text/plain(UTF-8)`
      responseAs[String] shouldBe s"Checking account $badId not found"
    }
  }

  val mmBalances = MoneyMarketAccountBalances(Some(List((goodId, 11000.10))))
  val mmPath = "/account/balances/mm/"
  val mmQ = mmPath ++ balancesQuery
  val badMmQ = mmPath ++ badBalancesQuery

  it should "return existing Money Market Account Balances" in {
    Get(mmQ) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[MoneyMarketAccountBalances[BigDecimal]] shouldBe mmBalances
    }
  }

  it should "return Error message for non-existing Money Market Balances" in {
    Get(badMmQ) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `text/plain(UTF-8)`
      responseAs[String] shouldBe s"Money Market account $badId not found"
    }
  }
  
  val saBalances = SavingsAccountBalances(Some(List((goodId, 111000.10))))
  val saPath = "/account/balances/savings/"
  val saQ = saPath ++ balancesQuery
  val badSaQ = saPath ++ badBalancesQuery
  
  it should "return existing Savings Account Balances" in {
    Get(saQ) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[SavingsAccountBalances[BigDecimal]] shouldBe saBalances
    }
  }

  it should "return Error message for non-existing Savings Balances" in {
    Get(badSaQ) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `text/plain(UTF-8)`
      responseAs[String] shouldBe s"Savings account $badId not found"
    }
  }
  
  it should "respond with handled = false for partial path" in {
    Get(saPath) ~> routes ~> check {
      handled shouldEqual false
    }
  }
}
