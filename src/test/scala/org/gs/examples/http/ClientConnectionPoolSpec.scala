package org.gs.examples.http

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.http.scaladsl.Http.HostConnectionPool

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ HttpMethod, HttpRequest, HttpResponse, RequestEntity }
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source }
import com.typesafe.config.{ Config, ConfigFactory }
import java.io.IOException
import org.gs.akka.http.ClientConnectionPool
import org.gs.akka.http._
import org.scalatest._
import org.scalatest.concurrent.{ PatienceConfiguration, ScalaFutures }
import org.scalatest.time.{ Seconds, Span }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }


class ClientConnectionPoolSpec extends FlatSpec with Matchers with ScalaFutures with Protocols with BeforeAndAfterAll {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val patience = PatienceConfiguration.Timeout(Span(3, Seconds))
  implicit val ec = ExecutionContext.Implicits.global

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  val flow = ClientConnectionPool(config.getString("http.interface"), config.getInt("http.port"))

  override def beforeAll() {
  }
  
  override def afterAll() {
  }
  type ReqFlow = Flow[(HttpRequest, Long), (Try[HttpResponse], Long), HostConnectionPool]
  def simpleRequest(uriStr: String, correlationId: Long, flow: ReqFlow):
          Future[(Try[HttpResponse], Long)] =
    Source.single(HttpRequest(uri = uriStr) -> correlationId)
      .via(flow)
      .runWith(Sink.head)

  def methodRequest(meth: HttpMethod, uriStr: String, correlationId: Long, flow: ReqFlow):
          Future[(Try[HttpResponse], Long)] =
    Source.single(HttpRequest(method = meth, uri = uriStr) -> correlationId)
      .via(flow)
      .runWith(Sink.head)

  def entityRequest(meth: HttpMethod,
                    uriStr: String,
                    ent: RequestEntity,
                    correlationId: Long,
                    flow: ReqFlow): Future[(Try[HttpResponse], Long)] =
    Source.single(HttpRequest(method = meth, uri = uriStr, entity = ent) -> correlationId)
      .via(flow)
      .runWith(Sink.head)

  val ip0Info = IpInfo("0.0.0.0", Option("No country"), Option("No city"), Option(0.0), Option(0.0))
  val ip1Info = IpInfo("8.8.8.8",
                       Option("United States"),
                       Option("Mountain View"),
                       Option(37.386),
                       Option(-122.0838))

  "A ClientConnectionPool" should "receive a response to a root request" in {
    val responseFuture = simpleRequest("/", 42L, flow)

    whenReady(responseFuture, patience) { result =>
      println(s"result._1:${result._1}")
      result._2 should equal(42)
    }
  }

  it should "receive a response to an ipInfo simple request" in {
    val responseFuture = simpleRequest(s"/ip/${ip1Info.ip}", 43L, flow)

    whenReady(responseFuture, patience) { result =>
      val response = result._1.get
      result._2 should equal(43)
      response.status match {
        case OK         => Unmarshal(response.entity).to[IpInfo].map(Right(_))
        case BadRequest => Future.successful(Left(s"${ip1Info.ip}: incorrect IP format"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Telize request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }

  it should "receive a response to an ipInfo ip Get request" in {
    val responseFuture = methodRequest(GET, s"/ip/${ip1Info.ip}", 44L, flow)

    whenReady(responseFuture, patience) { result =>
      val response = result._1.get
      println(s"response status:${response.status} headers:${response.headers} entity:${response.entity}")
      result._2 should equal(44)
      response.status match {
        case OK         => Unmarshal(response.entity).to[IpInfo].map(Right(_))
        case BadRequest => Future.successful(Left(s"${ip1Info.ip}: incorrect IP format"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Telize request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }
}

