package org.gs.examples.account.http

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.{ Config, ConfigFactory }
import org.gs._
import org.gs.akka.http._
import org.gs.examples.account._
import org.gs.http._

class CheckingBalancesClient()(implicit val system: ActorSystem, val mat: ActorMaterializer) extends
        BalancesClients {
  override implicit val materializer = mat
  val hostConfig = getHostConfig("akka-aggregator.checking-balances.http.interface",
          "akka-aggregator.checking-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("akka-aggregator.checking-balances.http.path", hostConfig)
}
/*
object CheckingBalancesClient {
  implicit val system = ActorSystem("akka-aggregator")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  def apply(): CheckingBalancesClient = {

    new CheckingBalancesClient()
  }

  def main(args: Array[String]): Unit = {
    implicit val logger = Logging(system, getClass)
    val client = CheckingBalancesClient()
    val hostConfig = client.hostConfig
    val flow = ClientConnectionPool(hostConfig._2, hostConfig._3)
    val f = client.requestCheckingBalances(1L, client.baseURL, client.mapChecking)
    implicit val executor = system.dispatcher
    f.onComplete {
      case Success(x) => {
        println(s"Success checkingBalances:$x")
      }
      case Failure(e) => println(s"FAIL ${e.getMessage}")
    }
  }
}
*/