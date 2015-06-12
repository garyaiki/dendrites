/**
  */
package org.gs.akka.http

import scala.concurrent.Future
import scala.util.Try
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl._
//import akka.http.{ ConnectionPoolSettings, ConnectionPoolSetup }
//import akka.http.ConnectionPoolSetup._
import akka.http.scaladsl.{ Http, HttpExt }
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.{ HttpResponse, HttpRequest }
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

/** Create connection pool with default configuration
  *
  * @author garystruthers
  *
  */
object ClientConnectionPool {
  implicit val system = ActorSystem()
  implicit val materializer = ActorFlowMaterializer()

  /** pool factory
    * @param host
    * @param port
    * @return connection pool
    */
  def apply(host: String, port: Int): Flow[(HttpRequest, Long), (Try[HttpResponse], Long), HostConnectionPool] = {
    val httpExt = new HttpExt(ConfigFactory.load())
    httpExt.cachedHostConnectionPool[Long](host, port)
  }
}