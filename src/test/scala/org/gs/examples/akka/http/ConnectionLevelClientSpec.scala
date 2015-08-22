package org.gs.examples.akka.http

import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow
import org.scalatest._

class ConnectionLevelClientSpec extends FlatSpec with Matchers with ScalatestRouteTest {
  override def testConfigSource = "akka.loglevel = WARNING"



  "A ConnectionLevelClient" should "respond with bad request on incorrect IP format" in {
   val client = new ConnectionLevelClient()
   
   
   val fut = client.responseFuture
   val flow = client.connectionFlow
   val complete = fut.isCompleted
   val response = client.responseFuture.value
   println(s"complete:$complete response:$response")

  }
}
