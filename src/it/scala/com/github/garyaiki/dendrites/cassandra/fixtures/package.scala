/**
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.github.garyaiki.dendrites.cassandra

import akka.stream.testkit.{TestPublisher, TestSubscriber}
import com.datastax.driver.core.{Cluster, ResultSet, Row, Session }
import com.datastax.driver.core.policies.{DefaultRetryPolicy, ExponentialReconnectionPolicy, LoggingRetryPolicy}
import java.util.UUID

package object fixtures {

  def buildCluster(config: CassandraConfig): Cluster = {
    val addresses = config.getInetAddresses()
    val retryPolicy = new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE)
    val reConnectPolicy = new ExponentialReconnectionPolicy(10L, 10L)
    val cluster = createCluster(addresses, retryPolicy, reConnectPolicy)
    val lbp = createLoadBalancingPolicy(config.localDataCenter)
    initLoadBalancingPolicy(cluster, lbp)
    logMetadata(cluster)
    registerQueryLogger(cluster)
    cluster.init() //DEBUG
    cluster
  }

  def getOneRow(id: UUID, pubSub: (TestPublisher.Probe[UUID], TestSubscriber.Probe[ResultSet])): Row = {
    pubSub._2.request(1)
    pubSub._1.sendNext(id)
    val response = pubSub._2.expectNext()
    response.one
  }
}