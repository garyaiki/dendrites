package org.gs.examples.account.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import org.gs.http._

class CheckingBalancesClientConfig()
        (implicit val system: ActorSystem, val mat: ActorMaterializer) extends BalancesProtocols {
  override implicit val materializer = mat
  val hostConfig = getHostConfig("akka-aggregator.checking-balances.http.interface",
    "akka-aggregator.checking-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("akka-aggregator.checking-balances.http.path", hostConfig)
}

object CheckingBalancesClientConfig {
  implicit val system = ActorSystem("akka-aggregator")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  def apply(): CheckingBalancesClientConfig = {
    new CheckingBalancesClientConfig()
  }

  def main(args: Array[String]): Unit = {
    val client = CheckingBalancesClientConfig()
    val hostConfig = client.hostConfig
    import org.gs.akka.http._
    val flow = ClientConnectionPool(hostConfig._2, hostConfig._3)
    val id = 1L
    import org.gs.examples.account._
    val callFuture = HigherOrderCalls.call(GetAccountBalances(id), client.baseURL)
    val responseFuture = HigherOrderCalls.byId(id, callFuture, client.mapChecking, client.mapPlain)
    implicit val executor = system.dispatcher
    import scala.util.{ Failure, Success, Try }
    responseFuture.onComplete {
      case Success(x) => {
        println(s"Success checkingBalances:$x")
      }
      case Failure(e) => println(s"FAIL ${e.getMessage}")
    }
  }
}
