
package org.gs

import _root_.akka.actor.ActorSystem
import _root_.akka.event.LoggingAdapter
import _root_.akka.http.scaladsl.Http
import _root_.akka.http.scaladsl.model.{ HttpEntity, HttpResponse, HttpRequest }
import _root_.akka.http.scaladsl.model.StatusCodes._
import _root_.akka.http.scaladsl.unmarshalling.Unmarshal
import _root_.akka.stream.Materializer
import com.typesafe.config.{ Config, ConfigFactory }
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.concurrent.ExecutionContext.Implicits.global

/** Provides Class to create an HostConnectionPool. Also functions to create requests and handle
  * response
  *
  * Get Config, ip address, port as tuple3
  * {{{
  * val hostConfig = getHostConfig("dendrites.checking-balances.http.interface","my.http.port")
  * }}}
  * Append path to host URL
  * {{{
  * val baseURL = configBaseUrl("my.http.path", hostConfig)
  * }}}
  * Create StringBuilder with URL including path 
  * {{{
  * val url = createUrl(scheme, ipDomain, port, path)
  * }}}
  * Create StringBuilder with request path i.e. "?", "key=value" for all fields in case class
  * {{{
  * val balanceQuery = GetAccountBalances(goodId)
  * val checkingPath = "/account/balances/checking/"
  * val balancesQuery = caseClassToGetQuery(balanceQuery)()
  * val q = checkingPath ++ balancesQuery
  * }}}
  * Construct GET request, call server using connection pool with typedQuery
  * typedResponse maps future to error message or case class
  * {{{
  * def receive = {
  *  case GetAccountBalances(id: Long) ⇒ {
  *    val callFuture = typedQuery(GetAccountBalances(id), clientConfig.baseURL)
  *    typedResponse(callFuture, mapPlain, mapChecking) pipeTo sender
  * }
  * }}}
  * typedQueryResponse wraps typedQuery and typedResponse. It has 2 argument lists, the first can be
  * curried. Then it can be used in a flow which receives the case class for the 2nd argument list
  * {{{
  * def partial = typedQueryResponse(baseURL, mapPlain, mapChecking) _ // curried
  * def flow: Flow[Product, Either[String, AnyRef], NotUsed] = Flow[Product].mapAsync(1)(partial)
  * }}}
  * @see [[http://typesafehub.github.io/config/latest/api/ Config API]]
  * @author Gary Struthers
  */
package object http {

  /** Get host URL config from a Config
		*
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
		* @param pathPath config key
    * @param hostConfig config plus ip address and port number
    * @param scheme "http" (default) or "https"
    * @return URL string for host/path 
    */
  def configBaseUrl(pathPath: String, hostConfig: (Config, String, Int), scheme: String = "http"):
          StringBuilder = {
    val config = hostConfig._1
    val ip = hostConfig._2
    val port = hostConfig._3
    val path = config.getString(pathPath)
    createUrl(scheme, ip, port, path)
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
    val goodDomain = if (domainPattern.findFirstIn(domain).isDefined ||
        ipPattern.findFirstIn(domain).isDefined) true else false
    require(goodDomain, s"domain:$domain looks invalid")
    val portRange = 0 to 65536
    require(portRange.contains(port), s"port:$port must be ${portRange.start} to ${portRange.end}")
    val pathPattern = """\/[/.a-zA-Z0-9-]+""".r
    require(pathPattern.findFirstIn(path).isDefined, s"path:path looks invalid")
    new StringBuilder(scheme).append("://").append(domain).append(':').append(port).append(path)
  }

    /** Get path from Config append to host URL
		*
		* @param pathPath requestPath config key
    * @param config
    * @return requestPath 
    */
  def configRequestPath(pathPath: String, config: Config): String = config.getString(pathPath)

  /** Transform case class to http GET query
    *  
    * @param cc case class or tuple
    * @param requestPath last part of path before '?'
    * @return field names and values as GET query string preceded with request path? 
    */
  def caseClassToGetQuery(cc: Product, requestPath: String): StringBuilder = {
    val sb = new StringBuilder(requestPath)
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
		* @see [[http://doc.akka.io/api/akka/2.4.7/#akka.http.scaladsl.Http$ "Http"]]
		* 
    * @param baseURL
    * @param requestPath
    * @param ccToGet function to map case class to requestPath and Get request
    * @param cc case class, in 2nd argument list so it can be curried
    * @param system implicit ActorSystem
    * @param materializer implicit Materializer
    * @return Future[HttpResponse]
    */
  def typedQuery(baseURL: StringBuilder,
                 requestPath: String,
                 ccToGet:(Product, String) => StringBuilder)
                (cc: Product)
                (implicit system: ActorSystem, materializer: Materializer): Future[HttpResponse] = {
    val balancesQuery = ccToGet(cc, requestPath)
    val uriS = (baseURL ++ balancesQuery).mkString
    Http().singleRequest(HttpRequest(uri = uriS))
  }

  /**	Map HttpResponse to a Future[Either] Left for error, Right for good result
		*
	  * @see [[http://doc.akka.io/api/akka/2.4.7/#akka.http.scaladsl.model.HttpResponse "HttpResponse"]]
	  * @see [[http://doc.akka.io/api/akka/2.4.7/#akka.http.scaladsl.unmarshalling.Unmarshal "Unmarshal"]]
		* @example [[org.gs.examples.account.http.actor.CheckingAccountClient]]
	  * 
    * @param mapLeft plain text response to Left
    * @param mapRight json response to Right
	  * @param caller future returned by query in 2nd arg list so it can be curried
    * @param system implicit ActorSystem
    * @param logger implicit LoggingAdapter
    * @param materializer implicit Materializer
    * @return Future[Either[String, AnyRef]]
    */
  def typedResponse(mapLeft: (HttpEntity) => Future[Left[String, Nothing]], 
                    mapRight: (HttpEntity) => Future[Right[String, AnyRef]])
        (response: HttpResponse)
        (implicit system: ActorSystem, logger: LoggingAdapter, materializer: Materializer): 
                   Future[Either[String, AnyRef]] = {

      response.status match {
        case OK => {
          val st = response.entity.contentType.mediaType.subType
          st match {
            case "json"  => mapRight(response.entity)
            case "plain" => mapLeft(response.entity)
          }
        }
        case BadRequest => Future.successful(Left(s"FAIL bad request:${response.status}"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"FAIL ${response.status} $entity"
          logger.error(error)
          Unmarshal(error).to[String].map(Left(_))
        }
      }
  }

  /**	Map Future[HttpResponse} to a Future[Either] Left for error, Right for good result
		*
	  * @see [[http://doc.akka.io/api/akka/2.4.7/#akka.http.scaladsl.model.HttpResponse "HttpResponse"]]
	  * @see [[http://doc.akka.io/api/akka/2.4.7/#akka.http.scaladsl.unmarshalling.Unmarshal "Unmarshal"]]
		* @example [[org.gs.examples.account.http.actor.CheckingAccountClient]]
	  * 
    * @param mapLeft plain text response to Left
    * @param mapRight json response to Right
	  * @param caller future returned by query in 2nd arg list so it can be curried
    * @param system implicit ActorSystem
    * @param logger implicit LoggingAdapter
    * @param materializer implicit Materializer
    * @return Future[Either[String, AnyRef]]
    */
def typedFutureResponse(mapLeft: (HttpEntity) => Future[Left[String, Nothing]], 
                        mapRight: (HttpEntity) => Future[Right[String, AnyRef]])
      (caller: Future[HttpResponse])
      (implicit system: ActorSystem, logger: LoggingAdapter, materializer: Materializer):
                    Future[Either[String, AnyRef]] = {

    caller.flatMap { response => typedResponse(mapLeft, mapRight)(response) }
  }

  /** Query server, map response
    *
    * Create a Partial Function by initializing first parameter list
    *
    * @param baseURL
    * @param requestPath
    * @param ccToGet function to map case class to requestPath and Get request
    * @param mapLeft plain text response to Left
    * @param mapRight json response to Right
    * @param cc case class mapped to GET query in 2nd arg list for currying
    * @param system implicit ActorSystem
    * @param logger implicit LoggingAdapter
    * @param materializer implicit Materializer
    * @return Future[Either[String, AnyRef]]
    */
  def typedQueryResponse(baseURL: StringBuilder,
               requestPath: String,
               ccToGet:(Product, String) => StringBuilder,
               mapLeft: (HttpEntity) => Future[Left[String, Nothing]], 
               mapRight: (HttpEntity) => Future[Right[String, AnyRef]])
              (cc: Product)
              (implicit system: ActorSystem, logger: LoggingAdapter, materializer: Materializer): 
               Future[Either[String, AnyRef]] = {
    val callFuture = typedQuery(baseURL, requestPath, ccToGet)(cc)
    typedFutureResponse(mapLeft, mapRight)(callFuture)
  }
}
