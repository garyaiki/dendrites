/**

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
package com.github.garyaiki.dendrites.http.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpResponse
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import scala.concurrent.Future
import com.github.garyaiki.dendrites.http.typedQuery

/** Send query to Http service. typedQuery builds a GET request and calls the server,
  * typedQuery is a curried function, only its initial argument list is passed here, the stream
  * passes the rest. Flow mapAsync calls curried typedQuery and passes 1 Future, the HTTP Response
  * downstream
  *
  * @constructor inits first arg list of typedQuery
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
  reqPath: String,
  ccToGet:(Product, String) => StringBuilder)
  (implicit val system: ActorSystem, logger: LoggingAdapter, val materializer: Materializer) {

  def partial: Product => Future[HttpResponse] = typedQuery(baseURL, reqPath, ccToGet) _ // curried

  def flow: Flow[Product, HttpResponse, NotUsed] = Flow[Product].mapAsync(1)(partial)
}
