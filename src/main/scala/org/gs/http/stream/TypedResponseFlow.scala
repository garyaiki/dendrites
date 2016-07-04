package org.gs.http.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{HttpResponse, HttpEntity}
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import scala.concurrent.Future
import org.gs.http.typedResponse

/** Map HttpResponse to Future[Either] Left for errors, Right case class for good result
  *
  * typedResponse is a curried function, only its initial argument list is passed here, the stream
  * passes the rest. Flow mapAsync calls curried typedResponse and passes 1 Future, the Either
  * response value or error message downstream
  *
  * @param mapLeft maps a failure message to Left
  * @param mapRight maps good result to Right.
  *
  * @author Gary Struthers
  *
  */
class TypedResponseFlow(mapLeft: (HttpEntity) => Future[Left[String, Nothing]],
                         mapRight: (HttpEntity) => Future[Right[String, AnyRef]])
        (implicit val system: ActorSystem, logger: LoggingAdapter, val materializer: Materializer) {

  def partial: HttpResponse => Future[Either[String, AnyRef]] =
        typedResponse(mapLeft, mapRight) _ // curried

  def flow: Flow[HttpResponse, Either[String, AnyRef], NotUsed] =
    Flow[HttpResponse].mapAsync(1)(partial)
}
