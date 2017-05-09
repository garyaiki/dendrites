package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.stream

import akka.NotUsed
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{PreparedStatement, ResultSet}
import java.util.UUID
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import com.github.garyaiki.dendrites.avro4s.stream.Avro4sSerializer
import com.github.garyaiki.dendrites.cassandra.{getConditionalError, getKeyspacesNames}
import com.github.garyaiki.dendrites.cassandra.fixtures.BeforeAfterAllBuilder
import com.github.garyaiki.dendrites.cassandra.fixtures.getOneRow
import com.github.garyaiki.dendrites.cassandra.stream.{CassandraBoundQuery, CassandraMappedPaging}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.avro4s.Avro4sShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{CassandraShoppingCart,
  CassandraShoppingCartEvtLog, RetryConfig, ShoppingCartConfig}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCart.{bndQuery, mapRow}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.{bndQuery =>
  evtBndQuery, mapRows}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.kafka.ShoppingCartCmdProducer
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.fixtures.{ShoppingCartBehaviors, ShoppingCartCmdBuilder}
import com.github.garyaiki.dendrites.kafka.stream.KafkaSink
import com.github.garyaiki.dendrites.stream.SpyFlow

class ShoppingCartCmdAndEvtSpec extends WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAfterAllBuilder
  with ShoppingCartCmdBuilder with ShoppingCartBehaviors {

  var prepStmts: Map[String, PreparedStatement] = null
  val ap = ShoppingCartCmdProducer

  override def beforeAll() {
    val iter = cmds.to[collection.immutable.Iterable]
    val source = Source[ShoppingCartCmd](iter)
    val serializer = new Avro4sSerializer(Avro4sShoppingCartCmd.toBytes)
    val sink = KafkaSink[String, Array[Byte]](ap)
    source.via(serializer).runWith(sink)
    Thread.sleep(500) // wait for Kafka
    createClusterSchemaSession(ShoppingCartConfig, 1)
    CassandraShoppingCart.createTable(session, schema)
    CassandraShoppingCartEvtLog.createTable(session, schema)
    prepStmts = prepareStatements(session, schema)
    Thread.sleep(500) // wait for Cassandra
  }

  "A ShoppingCart Command and Event log" should {
    "write to Kafka shoppingcartcmd-topic" ignore {

      Thread.sleep(500) // wait for Kafka
    }

    "poll commands from Kafka, execute them and save events to Cassandra" in {
      //val rg = shoppingCartCmdRG(dispatcher, session, prepStmts)
      val cmdEvtSource = shoppingCartCmdEvtSource(dispatcher)
      //val spy = new SpyFlow[(String, ShoppingCartCmd)]("cmd spy", 0, 0)
      val cmdAndEvtSinks = shoppingCartCmdEvtSinks(dispatcher, session, prepStmts)
      val rg = cmdEvtSource.to(cmdAndEvtSinks)
      rg.run()
      Thread.sleep(500) // wait for Cassandra
    }

    "find updated ShoppingCartCmd in Cassandra" in {
      Thread.sleep(500) // wait for Cassandra
      val response = queryShoppingCart(session, prepStmts)
      val shoppingCart = response(0)
      shoppingCart.cartId shouldBe cartId
      shoppingCart.owner shouldBe firstOwner
      val items = shoppingCart.items
      items.get(firstItem) match {
        case Some(x) => x shouldBe 1
        case None    => fail(s"ShoppingCart firstItem:$firstItem not found")
      }
      items.get(secondItem) match {
        case Some(x) => x shouldBe 2
        case None    => fail(s"ShoppingCart secondItem:secondItem not found")
      }
      shoppingCart.version shouldBe 9
    }

    "query by eventId and time" in {
      val response = queryShoppingCartEvent(session, prepStmts)
      response.length shouldBe kvCmds.length
    }
  }

  override def afterAll() {
    ap.producer.flush()
    val config = ConfigFactory.load()
    val closeTimeout = config.getLong("dendrites.kafka.close-timeout")
    Thread.sleep(closeTimeout)
    ap.producer.close(closeTimeout, scala.concurrent.duration.MILLISECONDS)
    dropSchemaCloseSessionCluster()
  }
}