package org.gs.http.stream

//import akka.NotUsed
//import akka.actor.ActorSystem
//import akka.event.LoggingAdapter
//import akka.http.scaladsl.model.{HttpResponse, HttpEntity}
//import akka.stream.Materializer
//import akka.stream.scaladsl.Flow
//import scala.concurrent.Future
//import org.gs.http.{caseClassToGetQuery, typedQuery, typedResponse}

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
class TypedQueryResponseFlow(query: TypedQueryFlow, responseHandler: TypedResponseFlow) {
//        (implicit val system: ActorSystem, logger: LoggingAdapter, val materializer: Materializer) {

  val flow = query.flow.via(responseHandler.flow)
}
