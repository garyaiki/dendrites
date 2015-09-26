/**
  */
package org.gs

import com.typesafe.config.{ Config, ConfigFactory }
import org.gs._

/** @author garystruthers
  *
  */
package object http {

    def getHostConfig(ipPath: String, portPath: String, config: Config = ConfigFactory.load()):
            (Config, String, Int) = {
    val ip = config.getString(ipPath)
    val port = config.getInt(portPath)
    (config, ip, port)
  }

  def configBaseUrl(pathPath: String, hostConfig: (Config, String, Int)): StringBuilder = {
    val config = hostConfig._1
    val ip = hostConfig._2
    val port = hostConfig._3
    val path = config.getString(pathPath)
    createUrl("http", ip, port, path)
  }
  
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
    *  @param cc case class (Product is supertype) 
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

}