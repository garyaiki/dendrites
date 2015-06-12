package org.gs.examples.account.akkahttp

import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow
import org.scalatest._
import org.gs.examples.account.{CheckingAccountBalances, GetAccountBalances, 
  MoneyMarketAccountBalances, SavingsAccountBalances}

class BalancesServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with BalancesService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  "BalancesService" should "respond to single slash query" in {
    Get(s"/") ~> routes ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "root"
    }
  }
  
  it should "return existing Checking Account Balances" in {
    Post(s"/account/balances/checking", GetAccountBalances(1L)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[CheckingAccountBalances] shouldBe CheckingAccountBalances(Some(List((1,1000.1))))
    }
  }
  
  it should "return Error message for non-existing Checking Account Balances" in {
    val badId = 4L
    Post(s"/account/balances/checking", GetAccountBalances(badId)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `text/plain(UTF-8)`
      responseAs[String] shouldBe s"Checking account $badId not found"
    }
  }
    
  it should "return existing Money Market Account Balances" in {
    Post(s"/account/balances/mm", GetAccountBalances(1L)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[MoneyMarketAccountBalances] shouldBe MoneyMarketAccountBalances(Some(List((1,11000.10))))
    }
  }
  
  it should "return Error message for non-existing Money Market Balances" in {
    val badId = 4L
    Post(s"/account/balances/mm", GetAccountBalances(badId)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `text/plain(UTF-8)`
      responseAs[String] shouldBe s"Money Market account $badId not found"
    }
  }
    
  it should "return existing Savings Account Balances" in {
    Post(s"/account/balances/savings", GetAccountBalances(1L)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[SavingsAccountBalances] shouldBe SavingsAccountBalances(Some(List((1,111000.10))))
    }
  }
  
  it should "return Error message for non-existing Savings Balances" in {
    val badId = 4L
    Post(s"/account/balances/savings", GetAccountBalances(badId)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `text/plain(UTF-8)`
      responseAs[String] shouldBe s"Savings account $badId not found"
    }
  }
/*
  it should "respond with bad request on incorrect IP format" in {
    Get("/ip/asdfg") ~> routes ~> check {
      status shouldBe BadRequest
      responseAs[String].length should be > 0
    }

    Post(s"/ip", IpPairSummaryRequest(ip1Info.ip, "asdfg")) ~> routes ~> check {
      status shouldBe BadRequest
      responseAs[String].length should be > 0
    }

    Post(s"/ip", IpPairSummaryRequest("asdfg", ip1Info.ip)) ~> routes ~> check {
      status shouldBe BadRequest
      responseAs[String].length should be > 0
    }
  }*/
}
