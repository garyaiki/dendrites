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
import org.gs.akka.http._
import org.gs.examples.account._
import org.gs.http._

class CheckingBalancesClient()(implicit val system: ActorSystem, val mat: ActorMaterializer) extends
        BalancesClients {
  import CheckingBalancesClient._
  val hostConfig = CheckingBalancesClient.getHostConfig()
  val config = hostConfig._1  
  override implicit val materializer = ActorMaterializer()
  override val logger = Logging(system, getClass)
}

object CheckingBalancesClient {
  implicit val system = ActorSystem("akka-aggregator")
  implicit val materializer = ActorMaterializer()

  def getHostConfig(config: Config = ConfigFactory.load()): (Config, String, Int) = {
    val ip = config.getString("akka-aggregator.checking-balances.http.interface")
    val port = config.getInt("akka-aggregator.checking-balances.http.port")
    (config, ip, port)
  }

  def configBaseUrl(hostConfig: (Config, String, Int)): StringBuilder = {
    val config = hostConfig._1
    val ip = hostConfig._2
    val port = hostConfig._3
    val path = config.getString("akka-aggregator.checking-balances.http.path")
    createUrl("http", ip, port, path)
  }

  def apply(): CheckingBalancesClient = {

    new CheckingBalancesClient()
  }

  def main(args: Array[String]): Unit = {

    val hostConfig = CheckingBalancesClient.getHostConfig()
    val flow = ClientConnectionPool(hostConfig._2, hostConfig._3)
    val client = CheckingBalancesClient()
    val f = client.requestCheckingBalances(1L, CheckingBalancesClient.configBaseUrl(hostConfig))
    implicit val executor = system.dispatcher
    f.onComplete {
      case Success(x) => {
        println(s"Success checkingBalances:$x")
      }
      case Failure(e) => println(s"FAIL ${e.getMessage}")
    }
  }

}