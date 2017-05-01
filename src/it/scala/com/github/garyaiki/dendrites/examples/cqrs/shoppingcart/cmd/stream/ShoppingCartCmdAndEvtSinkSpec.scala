package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{BoundStatement, Cluster, ConsistencyLevel, PreparedStatement, ResultSet, Row, Session}
import com.datastax.driver.core.policies.{DefaultRetryPolicy, LoggingRetryPolicy}
import com.datastax.driver.core.utils.UUIDs.timeBased
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.ExecutionContext
import com.github.garyaiki.dendrites.cassandra.{close, connect, createSchema, dropSchema, getConditionalError,
  getKeyspacesNames, sessionLogInfo}
import com.github.garyaiki.dendrites.cassandra.fixtures.{buildCluster, getOneRow}
import com.github.garyaiki.dendrites.cassandra.stream.{CassandraBoundQuery, CassandraMappedPaging}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{CassandraShoppingCart,
  CassandraShoppingCartEvtLog, RetryConfig, ShoppingCartConfig}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.{bndQuery,
  mapRows, prepQuery}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt

class ShoppingCartCmdAndEvtSinkSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val dispatcher = ActorAttributes.dispatcher("dendrites.blocking-dispatcher")
  val startTime = timeBased
  val myConfig = ShoppingCartConfig
  val schema = myConfig.keySpace
  var cluster: Cluster = null
  var session: Session = null
  val cartId = UUID.randomUUID
  val firstOwner = UUID.randomUUID
  val secondOwner = UUID.randomUUID
  val firstItem = UUID.randomUUID
  val secondItem = UUID.randomUUID
  val kvCmds = Seq((UUID.randomUUID.toString, ShoppingCartCmd("Insert", cartId, firstOwner, None)),
    (UUID.randomUUID.toString, ShoppingCartCmd("SetOwner", cartId, secondOwner, None)),
    (UUID.randomUUID.toString, ShoppingCartCmd("AddItem", cartId, firstItem, Some(1))),
    (UUID.randomUUID.toString, ShoppingCartCmd("AddItem", cartId, secondItem, Some(1))),
    (UUID.randomUUID.toString, ShoppingCartCmd("AddItem", cartId, firstItem, Some(1))),
    (UUID.randomUUID.toString, ShoppingCartCmd("AddItem", cartId, secondItem, Some(1))),
    (UUID.randomUUID.toString, ShoppingCartCmd("AddItem", cartId, secondItem, Some(1))),
    (UUID.randomUUID.toString, ShoppingCartCmd("SetOwner", cartId, firstOwner, None)),
    (UUID.randomUUID.toString, ShoppingCartCmd("RemoveItem", cartId, firstItem, Some(1))),
    (UUID.randomUUID.toString, ShoppingCartCmd("RemoveItem", cartId, secondItem, Some(1))))
  // Should be secondOwner, firstItem = 1, secondItem = 2
  var queryPrepStmt: PreparedStatement = null
  var evtQueryPrepStmt: PreparedStatement = null
  var prepStmts: Map[String, PreparedStatement] = null

  override def beforeAll() {
    cluster = buildCluster(myConfig)
    session = connect(cluster)
    val strategy = myConfig.replicationStrategy
    createSchema(session, schema, strategy, 1) // 1 instance
    val keyspacesStr = getKeyspacesNames(session)
    CassandraShoppingCart.createTable(session, schema)
    queryPrepStmt = CassandraShoppingCart.prepQuery(session, schema)
    CassandraShoppingCartEvtLog.createTable(session, schema)
    evtQueryPrepStmt = CassandraShoppingCartEvtLog.prepQuery(session, schema)
    val insPrepStmt = CassandraShoppingCart.prepInsert(session, schema)
    val delPrepStmt = CassandraShoppingCart.prepDelete(session, schema)
    val updateOwnerPrepStmt = CassandraShoppingCart.prepUpdateOwner(session, schema)
    val updateItemsPrepStmt = CassandraShoppingCart.prepUpdateItems(session, schema)
    val insEvtPrepStmt = CassandraShoppingCartEvtLog.prepInsert(session, schema)
    prepStmts = Map("Insert" -> insPrepStmt, "SetOwner" -> updateOwnerPrepStmt, "SetItem" -> updateItemsPrepStmt,
      "Delete" -> delPrepStmt, "Query" -> queryPrepStmt, "InsertEvt" -> insEvtPrepStmt)
  }

  "A ShoppingCart Command and Event log" should {

    "poll commands, execute them and save events to Cassandra" in {
      val iter = kvCmds.to[collection.immutable.Iterable]
      val source = Source[(String, ShoppingCartCmd)](iter)
      val sink = shoppingCartCmdEvtSink(dispatcher, session, prepStmts)
      val rg = source.to(sink)
      rg.run()
      Thread.sleep(500) // wait for Cassandra
    }

    "find updated ShoppingCartCmd in Cassandra" in {
      import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCart.{ bndQuery, mapRow }
      val source = TestSource.probe[UUID]
      val query = new CassandraBoundQuery[UUID](session, queryPrepStmt, bndQuery, 1)
      def sink = TestSink.probe[ResultSet]
      val (pub, sub) = source.via(query).toMat(sink)(Keep.both).run()
      val row = getOneRow(cartId, (pub, sub))
      pub.sendComplete()
      sub.expectComplete()

      val responseShoppingCart = mapRow(row)
      responseShoppingCart.cartId shouldBe cartId
      responseShoppingCart.owner shouldBe firstOwner
      val items = responseShoppingCart.items
      items.get(firstItem) match {
        case Some(x) => x shouldBe 1
        case None    => fail(s"ShoppingCart firstItem:$firstItem not found")
      }
      items.get(secondItem) match {
        case Some(x) => x shouldBe 2
        case None    => fail(s"ShoppingCart secondItem:secondItem not found")
      }
      responseShoppingCart.version shouldBe 9
    }

    "query by eventId and time" in {
      import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.{bndQuery,
        mapRows, prepQuery}
      val source = TestSource.probe[(UUID, UUID)]
      val bndStmt = new CassandraBoundQuery(session, evtQueryPrepStmt, bndQuery, 10)
      val paging = new CassandraMappedPaging[ShoppingCartEvt](10, mapRows)
      def sink = TestSink.probe[Seq[ShoppingCartEvt]]
      val (pub, sub) = source.via(bndStmt).via(paging).toMat(sink)(Keep.both).run()
      sub.request(1)
      pub.sendNext((cartId, startTime))
      var response = sub.expectNext()
      response.length shouldBe kvCmds.length
      pub.sendComplete()
      sub.expectComplete()
    }
  }

  override def afterAll() {
    dropSchema(session, schema)
    close(session, cluster)
  }
}