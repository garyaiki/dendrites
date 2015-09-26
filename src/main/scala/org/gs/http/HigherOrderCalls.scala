/**
  */
package org.gs.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpEntity, HttpResponse, HttpRequest }
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.concurrent.ExecutionContext.Implicits.global

/** @author garystruthers
  *
  */
object HigherOrderCalls {

  def call(cc: Product, baseUrl: StringBuilder)(implicit system: ActorSystem, materializer: Materializer): Future[HttpResponse] = {
    val balancesQuery = caseClassToGetQuery(cc)
    val uriS = (baseUrl ++ balancesQuery).mkString
    Http().singleRequest(HttpRequest(uri = uriS))
  }
  
  def byId(id: Long,
           caller: Future[HttpResponse],
           mapRight: (HttpEntity) => Future[Right[String, AnyRef]],
           mapLeft: (HttpEntity) => Future[Left[String, Nothing]])(implicit system: ActorSystem, logger: LoggingAdapter, materializer: Materializer): Future[Either[String, AnyRef]] = {

    caller.flatMap { response =>
      response.status match {
        case OK => {
          val st = response.entity.contentType.mediaType.subType
          st match {
            case "json"  => mapRight(response.entity)
            case "plain" => mapLeft(response.entity)
          }
        }
        case BadRequest => Future.successful(Left(s"FAIL id:$id bad request:${response.status}"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"FAIL id:$id ${response.status} $entity"
          logger.error(error)
          Unmarshal(error).to[String].map(Left(_))
        }
      }
    }
  }
}