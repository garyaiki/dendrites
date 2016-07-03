package org.gs.cassandra

import java.net.InetAddress
import java.util.{Collection => JCollection, List => JList}
import scala.collection.JavaConversions._

/** Configuration of Cassandra Java Driver 3.0+
  * 
  * @author Gary Struthers
  */
trait CassandraConfig {

  val ipAddresses: JList[String]
  val keySpace: String
  val replicationStrategy: String
  val localDataCenter: String

  /** Get cluster's node addresses
    * @see [[https://docs.oracle.com/javase/8/docs/api/index.html?java/net/InetAddress.html InetAddress]]
  	* @return Java Collection of node addresses
  	*/
  def getInetAddresses(): JCollection[InetAddress] = {
    val list = for {
      a <- ipAddresses
      i <- InetAddress.getAllByName(a)
    } yield(i)
    list.toSeq
  }
}
