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
package org.gs.http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.typesafe.config.ConfigFactory
import scala.util.Try

/** Factory for HostConnectionPool
  *
  * @throws [[http://doc.akka.io/api/akka/current/#akka.http.impl.engine.HttpConnectionTimeoutException HttpConnectionTimeoutException]]
  * @author Gary Struthers
  *
  */
object ClientConnectionPool {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  /** Create a pool that is started and cached but a connection isn't opened until a request arrives
    *
    * @param host
    * @param port
    * @return connection pool
    */
  def apply(host: String, port: Int):
          Flow[(HttpRequest, Long), (Try[HttpResponse], Long), HostConnectionPool] = {
    val httpExt = new HttpExt(ConfigFactory.load())
    httpExt.cachedHostConnectionPool[Long](host, port)
  }
}
