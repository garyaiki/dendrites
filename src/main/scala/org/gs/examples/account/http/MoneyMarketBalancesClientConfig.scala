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

class MoneyMarketBalancesClient()(implicit val system: ActorSystem, val mat: ActorMaterializer) extends
        BalancesClients {
  override implicit val materializer = mat
  val hostConfig = getHostConfig("akka-aggregator.money-market-balances.http.interface",
          "akka-aggregator.money-market-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("akka-aggregator.money-market-balances.http.path", hostConfig)
}
