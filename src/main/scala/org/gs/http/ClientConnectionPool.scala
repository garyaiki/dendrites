
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
