
package org.gs.akka

import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.{ HttpMethod, HttpRequest, HttpResponse, RequestEntity }
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.{ Flow, Sink, Source }

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

/** @author garystruthers
  *
  */
package object http {
  type ReqFlow = Flow[(HttpRequest, Long), (Try[HttpResponse], Long), HostConnectionPool]
  def simpleRequest(uriStr: String, correlationId: Long, flow: ReqFlow)(implicit flowMat: ActorFlowMaterializer): Future[(Try[HttpResponse], Long)] =
    Source.single(HttpRequest(uri = uriStr) -> correlationId)
      .via(flow)
      .runWith(Sink.head)

  def methodRequest(meth: HttpMethod, uriStr: String, correlationId: Long, flow: ReqFlow)(implicit flowMat: ActorFlowMaterializer): Future[(Try[HttpResponse], Long)] =
    Source.single(HttpRequest(method = meth, uri = uriStr) -> correlationId)
      .via(flow)
      .runWith(Sink.head)

  def entityRequest(meth: HttpMethod, uriStr: String, ent: RequestEntity, correlationId: Long, flow: ReqFlow)(implicit flowMat: ActorFlowMaterializer): Future[(Try[HttpResponse], Long)] =
    Source.single(HttpRequest(method = meth, uri = uriStr, entity = ent) -> correlationId)
      .via(flow)
      .runWith(Sink.head)
}