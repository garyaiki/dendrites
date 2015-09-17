package org.gs.examples.account.http

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import com.typesafe.config.{ Config, ConfigFactory }
import org.gs.akka.http._
import org.gs.examples.account._
import org.gs.http._

class CheckingBalancesClient()(implicit val system: ActorSystem, val mat: ActorMaterializer) extends BalancesProtocols {

  import system.dispatcher

  def call(id: Long, baseUrl: StringBuilder): Future[HttpResponse] = {
    val balancesQuery = caseClassToGetQuery(GetAccountBalances(id))
    val uriS = baseUrl.append(balancesQuery).toString
    Http().singleRequest(HttpRequest(uri = uriS))
  }

  def extractResponse(f: Future[HttpResponse]) {
    f.onSuccess {
      case r: HttpResponse => println(s"response:$r status:${r.status} entitity:${r.entity}")
    }
    f.onFailure {
      case x => println(s"Client failed $x")
    }
  }
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

    val f = client.call(1L, CheckingBalancesClient.configBaseUrl(hostConfig))
    client.extractResponse(f)
  }

}