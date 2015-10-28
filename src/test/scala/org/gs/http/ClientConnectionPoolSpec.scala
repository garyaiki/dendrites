package org.gs.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ HttpMethod, HttpRequest, HttpResponse, RequestEntity }
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source }
import com.typesafe.config.ConfigFactory
import java.io.IOException
import org.gs.http.ClientConnectionPool
import org.gs.examples.account.CheckingAccountBalances
import org.gs.examples.account.http.BalancesProtocols
import org.scalatest._
import org.scalatest.concurrent.{ PatienceConfiguration, ScalaFutures }
import org.scalatest.time.{ Seconds, Span }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try
import akka.http.scaladsl.model.Uri.apply
import scala.Right


class ClientConnectionPoolSpec extends FlatSpec
                               with Matchers
                               with ScalaFutures
                               with BalancesProtocols {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val patience = PatienceConfiguration.Timeout(Span(3, Seconds))
  implicit val ec = ExecutionContext.Implicits.global

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  val flow = ClientConnectionPool(config.getString("http.interface"), config.getInt("http.port"))

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

  "A ClientConnectionPool" should "receive a response to a root request" in {
    val responseFuture = simpleRequest("/", 42L, flow)

    whenReady(responseFuture, patience) { result =>
      result._2 should equal(42)
    }
  }

  it should "receive a response to a GetAccountBalances simple request" in {
    val responseFuture = simpleRequest(
            s"/account/balances/checking/GetAccountBalances?id=1", 1L, flow)

    whenReady(responseFuture, patience) { result =>
      val response = result._1.get
      result._2 should equal(1)
      response.status match {
        case OK         => Unmarshal(response.entity).to[CheckingAccountBalances].map(Right(_))
        case BadRequest => Future.successful(
                s"status:${response.status} headers:${response.headers} entity:${response.entity}")
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"GetAccountBalances request failed status:${response.status}, entity:$entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }

  it should "receive a response to a GetAccountBalances id Get request" in {
    val responseFuture = methodRequest(GET,
                                    s"/account/balances/checking/GetAccountBalances?id=2", 2L, flow)

    whenReady(responseFuture, patience) { result =>
      val response = result._1.get
      result._2 should equal(2)
      response.status match {
        case OK         => Unmarshal(response.entity).to[CheckingAccountBalances].map(Right(_))
        case BadRequest => Future.successful(
                s"status:${response.status} headers:${response.headers} entity:${response.entity}")
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"GetAccountBalances request failed status:${response.status}, entity:$entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }
}
