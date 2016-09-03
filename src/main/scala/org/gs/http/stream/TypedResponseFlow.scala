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
package org.gs.http.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshaller.NoContentException
import akka.http.scaladsl.unmarshalling.Unmarshaller.UnsupportedContentTypeException
import akka.stream.{ActorAttributes, Materializer, Supervision}
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.scaladsl.Flow
import scala.concurrent.{ExecutionContext, Future}
import org.gs.http.typedResponse

/** Map HttpResponse to Future[Either] Left for errors, Right case class for good result
  *
  * typedResponse is a curried function, only its initial argument list is passed here, the stream
  * passes the rest. Flow mapAsync calls curried typedResponse and passes 1 Future, the Either
  * response value or error message downstream
  *
  * @constructor Inits first arg list of typedResponse, passes it to mapAsync
  * @param mapLeft maps a failure message to Left
  * @param mapRight maps good result to Right.
  * @param system implicit ActorSystem
  * @param logger implicit LoggingAdapter
  * @param materializer implicit Materializer
  * @author Gary Struthers
  *
  */
class TypedResponseFlow(mapLeft: (HttpEntity) => Future[Left[String, Nothing]],
                         mapRight: (HttpEntity) => Future[Right[String, AnyRef]])
        (implicit val ec: ExecutionContext,
         system: ActorSystem,
         logger: LoggingAdapter,
         val materializer: Materializer) {

  def partial: HttpResponse => Future[Either[String, AnyRef]] =
        typedResponse(mapLeft, mapRight) _ // curried

  def flow: Flow[HttpResponse, Either[String, AnyRef], NotUsed] =
    Flow[HttpResponse].mapAsync(1)(partial)
}

object TypedResponseFlow {

  /** Supervision strategy for flows with Unmarshalling functions i.e. map*
    *
  	* @see [[http://doc.akka.io/api/akka/current/#akka.http.scaladsl.unmarshalling.Unmarshaller$$NoContentException$ NoContentException]]
  	*/ 
  def decider: Supervision.Decider = {
    case NoContentException => Supervision.Resume
    case _  => Supervision.Stop
  }
}
