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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{Cluster, ResultSet, Row, Session}
import com.datastax.driver.core.policies.{DefaultRetryPolicy, LoggingRetryPolicy}
import com.datastax.driver.core.utils.UUIDs.timeBased
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.immutable.Iterable
import scala.concurrent.ExecutionContext
import scala.util.Random
import com.github.garyaiki.dendrites.cassandra.stream.{CassandraBind, CassandraBoundQuery, CassandraMappedPaging,
  CassandraSink}
import com.github.garyaiki.dendrites.cassandra.{close, connect, createCluster, createLoadBalancingPolicy, createSchema,
  dropSchema, executeBoundStmt, initLoadBalancingPolicy, logMetadata, registerQueryLogger}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{ShoppingCartConfig,
  CassandraShoppingCartEvtLog}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.{bndInsert,
  bndQuery, createTable, mapRows, prepInsert, prepQuery, table}

class CassandraShoppingCartEvtLogSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val startTime = timeBased
  val myConfig = ShoppingCartConfig
  val schema = myConfig.keySpace
  var cluster: Cluster = null
  var session: Session = null
  val cartId = UUID.randomUUID
  val ownerId = UUID.randomUUID
  val newOwnerId = UUID.randomUUID
  val itemId = UUID.randomUUID
  val cartEvts = Seq(ShoppingCartEvt(cartId, timeBased, UUID.randomUUID, Some(ownerId), Some(itemId), Some(1)),
    ShoppingCartEvt(cartId, timeBased, UUID.randomUUID, None, Some(itemId), Some(1)),
    ShoppingCartEvt(cartId, timeBased, UUID.randomUUID, Some(newOwnerId), None, None))

  override def beforeAll() {
    val addresses = myConfig.getInetAddresses()
    val retryPolicy = new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE)
    cluster = createCluster(addresses, retryPolicy)
    val lbp = createLoadBalancingPolicy(myConfig.localDataCenter)
    initLoadBalancingPolicy(cluster, lbp)
    logMetadata(cluster)
    registerQueryLogger(cluster)
    session = connect(cluster)

    val strategy = myConfig.replicationStrategy
    val createSchemaRS = createSchema(session, schema, strategy, 3)
    val evtLogTable = createTable(session, schema)
  }

  "A Cassandra ShoppingCartEvtLog client" should {
    "insert ShoppingCartEvt " in {
      val iter = Iterable(cartEvts.toSeq: _*)
      val source = Source[ShoppingCartEvt](iter)
      val bndStmt = new CassandraBind(prepInsert(session, schema), bndInsert)
      val sink = new CassandraSink(session)
      source.via(bndStmt).runWith(sink)
    }
  }

  "query by eventId and time" in {
    val source = TestSource.probe[(UUID, UUID)]
    val bndStmt = new CassandraBoundQuery(session, prepQuery(session, schema), bndQuery, 10)
    val paging = new CassandraMappedPaging[ShoppingCartEvt](10, mapRows)
    def sink = TestSink.probe[Seq[ShoppingCartEvt]]
    val (pub, sub) = source.via(bndStmt).via(paging).toMat(sink)(Keep.both).run()
    sub.request(1)
    pub.sendNext((cartId, startTime))
    var response = sub.expectNext()
    response shouldBe cartEvts
    pub.sendComplete()
    sub.expectComplete()
  }

  override def afterAll() {
    dropSchema(session, schema)
    close(session, cluster)
  }
}
