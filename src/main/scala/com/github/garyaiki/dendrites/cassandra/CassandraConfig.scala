/** Copyright 2016 Gary Struthers

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.garyaiki.dendrites.cassandra

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
    *
    * @return Java Collection of node addresses
    * @see [[https://docs.oracle.com/javase/8/docs/api/index.html?java/net/InetAddress.html InetAddress]]
    */
  def getInetAddresses(): JCollection[InetAddress] = {
    val list = for {
      a <- ipAddresses
      i <- InetAddress.getAllByName(a)
    } yield(i)
    list.toSeq
  }
}
