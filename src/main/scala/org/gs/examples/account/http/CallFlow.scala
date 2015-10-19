/**
  */
package org.gs.examples.account.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpEntity, HttpResponse, HttpRequest }
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import org.gs._
import org.gs.http.HttpCalls
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.concurrent.ExecutionContext.Implicits.global

/** @author garystruthers
  *
  */
object  CallFlow  {
  def callById(baseURL: StringBuilder,
               mapLeft: (HttpEntity) => Future[Left[String, Nothing]], 
               mapRight: (HttpEntity) => Future[Right[String, AnyRef]])
               (cc: Product)
               (implicit system: ActorSystem, logger: LoggingAdapter, materializer: Materializer):
          Future[Either[String, AnyRef]] = {
    
    val callFuture = HttpCalls.call(cc, baseURL)
    val fields = ccToMap(cc).filterKeys(_ != "$outer")
    val id = fields.get("id") match {
      case Some(x) => x match {
        case x: Long => x
      }
    }
    HttpCalls.byId(id, callFuture, mapLeft, mapRight)
  }
}