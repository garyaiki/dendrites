/**
  */
package org.gs

import _root_.akka.actor.ActorSystem
import _root_.akka.event.LoggingAdapter
import _root_.akka.http.scaladsl.Http
import _root_.akka.http.scaladsl.model.{ HttpEntity, HttpResponse, HttpRequest }
import _root_.akka.http.scaladsl.model.StatusCodes._
import _root_.akka.http.scaladsl.unmarshalling.Unmarshal
import _root_.akka.stream.Materializer
import com.typesafe.config.{ Config, ConfigFactory }
import org.gs._
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.concurrent.ExecutionContext.Implicits.global

/** @author garystruthers
  *
  */
package object http {

  /** Get host URL config from a Config
		*
		* @see [[http://typesafehub.github.io/config/latest/api/]]
		* @param ipPath config key
    * @param portPath config key
    * @param config
    * @return config plus ip address and port number 
    */
  def getHostConfig(ipPath: String, portPath: String, config: Config = ConfigFactory.load()):
            (Config, String, Int) = {
    val ip = config.getString(ipPath)
    val port = config.getInt(portPath)
    (config, ip, port)
  }

  /** Get path from Config append to host URL
		*
		* @see [[http://typesafehub.github.io/config/latest/api/]]
		* @param pathPath config key
    * @param hostConfig config plus ip address and port number
    * @return URL string for host/path 
    */
  def configBaseUrl(pathPath: String, hostConfig: (Config, String, Int)): StringBuilder = {
    val config = hostConfig._1
    val ip = hostConfig._2
    val port = hostConfig._3
    val path = config.getString(pathPath)
    createUrl("http", ip, port, path)
  }

  /** Create URL string from components
    *
    * @param scheme http or https
    * @param domain IP address or domain name
    * @param port
    * @param path
    * @return StringBuilder
    */
  def createUrl(scheme: String, domain: String, port: Int, path: String): StringBuilder = {
    require(scheme == "http" || scheme == "https", s"scheme:$scheme must be http or https")
    val domainPattern = """[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9]\.[a-zA-Z]{2,}""".r
    val ipPattern = """\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}""".r
    val gd = if (domainPattern.findFirstIn(domain).isDefined ||
        ipPattern.findFirstIn(domain).isDefined) true else false
    require(gd, s"domain:$domain looks invalid")
    val r = 0 to 65536
    require(r.contains(port), s"port:$port must be ${r.start} to ${r.end}")
    val pathPattern = """\/[/.a-zA-Z0-9-]+""".r
    require(pathPattern.findFirstIn(path).isDefined, s"path:path looks invalid")
    new StringBuilder(scheme).append("://").append(domain).append(':').append(port).append(path)
  }
  
  /** Extract case class elements into http GET query
    *  
    *  @param cc case class or tuple; Product is supertype 
    *  @return field names and values as GET query string preceded with ccName? 
    */
  def caseClassToGetQuery(cc: Product): StringBuilder = {
    val sb = new StringBuilder(cc.productPrefix)
    sb.append('?')
    val fields = ccToMap(cc).filterKeys(_ != "$outer")
    fields.foreach{
      case (key, value) => sb.append(key).append('=').append(value).append('&')
    }
    if(sb.last == '&') sb.setLength(sb.length() - 1)
    sb
  }

  /** Call server with GET query, case class is turned into Get query, appended to baseURL
		*
		* @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.http.javadsl.Http]]
		* @example [[org.gs.examples.account.http.actor.CheckingAccountClient]]
		* 
    * @param cc case class 
    * @param baseURL
    * @param system implicit ActorSystem
    * @param materializer implicit Materializer
    * @return Future[HttpResponse]
    */
  def call(cc: Product, baseURL: StringBuilder)(implicit system: ActorSystem,
          materializer: Materializer): Future[HttpResponse] = {
    val balancesQuery = caseClassToGetQuery(cc)
    val uriS = (baseURL ++ balancesQuery).mkString
    Http().singleRequest(HttpRequest(uri = uriS))
  }
  
  /**	Map response
		*
		* @param id
	  * @param caller
    * @param mapLeft
    * @param mapRight
    * @param system
    * @param logger
    * @param materializer
    * @return
    */
def byId(id: Long, 
           caller: Future[HttpResponse], 
           mapLeft: (HttpEntity) => Future[Left[String, Nothing]], 
           mapRight: (HttpEntity) => Future[Right[String, AnyRef]])(
                    implicit system: ActorSystem, logger: LoggingAdapter, 
                              materializer: Materializer): Future[Either[String, AnyRef]] = {

    caller.flatMap { response =>
      response.status match {
        case OK => {
          val st = response.entity.contentType.mediaType.subType
          st match {
            case "json"  => mapRight(response.entity)
            case "plain" => mapLeft(response.entity)
          }
        }
        case BadRequest => Future.successful(Left(s"FAIL id:$id bad request:${response.status}"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"FAIL id:$id ${response.status} $entity"
          logger.error(error)
          Unmarshal(error).to[String].map(Left(_))
        }
      }
    }
  }


  def callById(baseURL: StringBuilder,
               mapLeft: (HttpEntity) => Future[Left[String, Nothing]], 
               mapRight: (HttpEntity) => Future[Right[String, AnyRef]])
               (cc: Product)
               (implicit system: ActorSystem, logger: LoggingAdapter, materializer: Materializer):
          Future[Either[String, AnyRef]] = {
    
    val callFuture = call(cc, baseURL)
    val fields = ccToMap(cc).filterKeys(_ != "$outer")
    val id = fields.get("id") match {
      case Some(x) => x match {
        case x: Long => x
      }
    }
    byId(id, callFuture, mapLeft, mapRight)
  }
}
