/**
  */
package org.gs.examples.account

import _root_.akka.actor.ActorSystem
import _root_.akka.event.LoggingAdapter
import _root_.akka.http.scaladsl.model.HttpEntity
import _root_.akka.stream.Materializer
import org.gs._
import org.gs.http._
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.concurrent.ExecutionContext.Implicits.global

/** @author garystruthers
  *
  */
package object http {
/*
  def callById(baseURL: StringBuilder,
               mapLeft: (HttpEntity) => Future[Left[String, Nothing]], 
               mapRight: (HttpEntity) => Future[Right[String, AnyRef]])
               (cc: Product)
               (implicit system: ActorSystem, logger: LoggingAdapter, materializer: Materializer):
          Future[Either[String, AnyRef]] = {
    
    val callFuture = call(cc, baseURL)
    val fields = ccToMap(cc).filterKeys(_ != "$outer")
    val id = fields.get("id") match {
      case Some(x) => x match {
        case x: Long => x
      }
    }
    byId(id, callFuture, mapLeft, mapRight)
  }
*/
  val checkingBalances = Map(
    1L -> Some(List(
      (1L, BigDecimal(1000.10)))),
    2L -> Some(List(
      (2L, BigDecimal(2000.20)),
      (22L, BigDecimal(2200.22)))),
    3L -> Some(List(
      (3L, BigDecimal(3000.30)),
      (33L, BigDecimal(3300.33)),
      (333L, BigDecimal(3330.33)))))

  val moneyMarketBalances = Map(
    1L -> Some(List(
      (1L, BigDecimal(11000.10)))),
    2L -> Some(List(
      (2L, BigDecimal(22000.20)),
      (22L, BigDecimal(22200.22)))),
    3L -> Some(List(
      (3L, BigDecimal(33000.30)),
      (33L, BigDecimal(33300.33)),
      (333L, BigDecimal(33330.33)))))
      

  val savingsBalances = Map(
    1L -> Some(List(
      (1L, BigDecimal(111000.10)))),
    2L -> Some(List(
      (2L, BigDecimal(222000.20)),
      (22L, BigDecimal(222200.22)))),
    3L -> Some(List(
      (3L, BigDecimal(333000.30)),
      (33L, BigDecimal(333300.33)),
      (333L, BigDecimal(333330.33)))))
}