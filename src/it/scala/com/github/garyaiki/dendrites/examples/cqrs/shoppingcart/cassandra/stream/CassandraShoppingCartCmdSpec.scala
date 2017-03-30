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
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{Cluster, ConsistencyLevel, PreparedStatement, ResultSet, Row, Session}
import com.datastax.driver.core.policies.{DefaultRetryPolicy, LoggingRetryPolicy}
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.immutable.Iterable
import scala.concurrent.ExecutionContext
import com.github.garyaiki.dendrites.cassandra.{close, connect, createCluster, createLoadBalancingPolicy, createSchema,
  dropSchema, getConditionalError, initLoadBalancingPolicy, logMetadata, registerQueryLogger}
import com.github.garyaiki.dendrites.cassandra.stream.{CassandraBind, CassandraBoundQuery, CassandraConditional,
  CassandraMappedPaging, CassandraPaging, CassandraQuery, CassandraRetrySink, CassandraSink}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.{ShoppingCart, SetItems, SetOwner}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{ShoppingCartConfig, CassandraShoppingCart}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCart.{bndDelete, bndInsert,
  bndQuery, bndUpdateItems, bndUpdateOwner, checkAndSetOwner, createTable, mapRow, mapRows, prepDelete, prepInsert, prepQuery,
  prepUpdateItems, prepUpdateOwner, rowToString, table}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.RetryConfig
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.doShoppingCartCmd
import com.github.garyaiki.dendrites.stream.SpyFlow

class CassandraShoppingCartCmdSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val myConfig = ShoppingCartConfig
  val schema = myConfig.keySpace
  var cluster: Cluster = null
  var session: Session = null
  var stmts: Map[String, PreparedStatement] = null
  val curriedDoCmd = doShoppingCartCmd(session, stmts) _
  val cartId = UUID.randomUUID
  val firstOwner = UUID.randomUUID
  val secondOwner = UUID.randomUUID
  val firstItem = UUID.randomUUID
  val secondItem = UUID.randomUUID
  var queryPrepStmt: PreparedStatement = null
  var delPrepStmt: PreparedStatement = null
  val cmds = Seq(ShoppingCartCmd("Insert", cartId, firstOwner, None),
    ShoppingCartCmd("SetOwner", cartId, secondOwner, None),
    ShoppingCartCmd("AddItem", cartId, firstItem, Some(1)),
    ShoppingCartCmd("AddItem", cartId, secondItem, Some(1)),
    ShoppingCartCmd("AddItem", cartId, firstItem, Some(1)),
    ShoppingCartCmd("AddItem", cartId, secondItem, Some(1)),
    ShoppingCartCmd("AddItem", cartId, secondItem, Some(1)),
    ShoppingCartCmd("SetOwner", cartId, firstOwner, None),
    ShoppingCartCmd("RemoveItem", cartId, firstItem, Some(1)),
    ShoppingCartCmd("RemoveItem", cartId, secondItem, Some(1)))
  // Should be secondOwner, firstItem = 1, secondItem = 2
  val dispatcher = ActorAttributes.dispatcher("dendrites.blocking-dispatcher")

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
    val createSchemaRS = createSchema(session, schema, strategy, 1) // 1 instance
    val cartTableRS = createTable(session, schema)
    delPrepStmt = prepDelete(session, schema)
    queryPrepStmt = prepQuery(session, schema)
    //queryPrepStmt.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
    stmts = Map("Insert" -> prepInsert(session, schema), "SetOwner" -> prepUpdateOwner(session, schema),
      "SetItem" -> prepUpdateItems(session, schema), "Delete" -> delPrepStmt, "Query" -> queryPrepStmt)
  }

  "A Cassandra ShoppingCartCmd client" should {
    "insert ShoppingCart, set owners & items " in {
      val curriedDoCmd = doShoppingCartCmd(session, stmts) _
      val iter = Iterable(cmds.toSeq: _*)
      val source = Source[ShoppingCartCmd](iter)
      //val spy = new SpyFlow[ShoppingCartCmd]("ShoppingCartCmd spy 1", 0, 0)

      val sink = new CassandraRetrySink[ShoppingCartCmd](RetryConfig, curriedDoCmd).withAttributes(dispatcher)
      source.runWith(sink)
    }
  }
/*
  "query a ShoppingCart after updating items and then owner" in {
    val source = TestSource.probe[UUID]
    val query = new CassandraBoundQuery[UUID](session, 1, queryPrepStmt, bndQuery)
    val paging = new CassandraMappedPaging[ShoppingCart](10, mapRows)
    def sink = TestSink.probe[Seq[ShoppingCart]]
    val (pub, sub) = source.via(query).via(paging).toMat(sink)(Keep.both).run()
    sub.request(1)
    pub.sendNext(cartId)
    val response = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()
    val responseShoppingCart = response(0)
    responseShoppingCart.cartId shouldBe cartId
    responseShoppingCart.owner shouldBe secondOwner
    val items = responseShoppingCart.items
    items.get(firstItem) match {
      case Some(x) => x shouldBe 1
      case None => fail(s"ShoppingCart firstItem:$firstItem not found")
    }
    items.get(secondItem) match {
      case Some(x) => x shouldBe 2
      case None => fail(s"ShoppingCart secondItem:secondItem not found")
    }
    responseShoppingCart.version shouldBe 9
    //response(0) shouldBe ShoppingCart(cartId, secondOwner, Map(firstItem -> 1, secondItem -> 2), 10)
  }
*/
  "query a ShoppingCart again after updating items and then owner" in {
    val source = TestSource.probe[UUID]
    val query = new CassandraBoundQuery[UUID](session, 1, queryPrepStmt, bndQuery)
    def sink = TestSink.probe[ResultSet]
    val (pub, sub) = source.via(query).toMat(sink)(Keep.both).run()
    sub.request(1)
    pub.sendNext(cartId)
    val response = sub.expectNext()
    val row = response.one
    pub.sendComplete()
    sub.expectComplete()

    val responseShoppingCart = mapRow(row)
    responseShoppingCart.cartId shouldBe cartId
    responseShoppingCart.owner shouldBe firstOwner
    val items = responseShoppingCart.items
    items.get(firstItem) match {
      case Some(x) => x shouldBe 1
      case None => fail(s"ShoppingCart firstItem:$firstItem not found")
    }
    items.get(secondItem) match {
      case Some(x) => x shouldBe 2
      case None => fail(s"ShoppingCart secondItem:secondItem not found")
    }
    responseShoppingCart.version shouldBe 9
  }

  "delete ShoppingCart " in {
    val cartIds = Seq(cartId)
    val iter = Iterable(cartIds.toSeq: _*)
    val source = Source[UUID](iter)
    val bndStmt = new CassandraBind(delPrepStmt, bndDelete)
    val sink = new CassandraSink(session)
    source.via(bndStmt).runWith(sink)
  }

  override def afterAll() {
    dropSchema(session, schema)
    close(session, cluster)
  }
}
