package org.gs.http.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpResponse
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import org.gs.http.typedQuery

/** Send query to Http service. typedQuery builds a GET request and calls the server,
  * typedQuery is a curried function, only its initial argument list is passed here, the stream
  * passes the rest. Flow mapAsync calls curried typedQuery and passes 1 Future, the HTTP Response
  * downstream
  *
  * @param baseURL
  * @param requestPath
  * @param ccToGet function to map case class to requestPath and Get request
  * @param system implicit ActorSystem
  * @param logger implicit LoggingAdapter
  * @param materializer implicit Materializer
  * 
  * @author Gary Struthers
  *
  */
class TypedQueryFlow(baseURL: StringBuilder,
        requestPath: String,
        ccToGet:(Product, String) => StringBuilder)
        (implicit val system: ActorSystem, logger: LoggingAdapter, val materializer: Materializer) {

  def partial = typedQuery(baseURL, requestPath, ccToGet) _ // curried

  def flow: Flow[Product, HttpResponse, NotUsed] = Flow[Product].mapAsync(1)(partial)
}
