package org.gs.cassandra.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.datastax.driver.core.{BoundStatement,Cluster,PreparedStatement,ResultSet,Row,Session}
import com.datastax.driver.core.policies.{DefaultRetryPolicy, LoggingRetryPolicy, RetryPolicy}
import java.util.{HashSet => JHashSet, UUID}
import org.scalatest._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import scala.collection.immutable.Iterable
import org.gs.cassandra._
import org.gs.cassandra.Playlists._

class CassandraPlaylistSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val myConfig = PlaylistSongConfig
  val schema = myConfig.keySpace
  var cluster: Cluster = null
  var session: Session = null
  val songId = UUID.fromString("756716f7-2e54-4715-9f00-91dcbea6cf50")
  val plId = UUID.fromString("2cc9ccb7-6221-4ccb-8387-f22b6a1b354d")
  val playlist = Playlist(plId,"La Petite Tonkinoise","Bye Bye Blackbird","Joséphine Baker",songId)
  val playlists = Seq(playlist)
  val playlistIds = Seq(plId)

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
    val songTRS = Playlists.createTable(session, schema)
  }

  "A Cassandra Playlist client" should {
    "insert Playlists " in {
      val iter = Iterable(playlists.toSeq:_*)
      val source = Source[Playlist](iter)
      val bndStmt = new CassandraBind(Playlists.playlistsPrepInsert(session, schema), playlistToBndInsert)
      val sink = new CassandraSink(session)
      source.via(bndStmt).runWith(sink)
    }
  }

  "query a Playlist" in {
      val source = TestSource.probe[UUID]
      val bndStmt = new CassandraBind(Playlists.playlistsPrepQuery(session, schema), playlistToBndQuery)
      val query = new CassandraQuery(session)
      val paging = new CassandraPaging(10)
      def toPlaylists: Flow[Seq[Row], Seq[Playlist], NotUsed] = Flow[Seq[Row]].map(Playlists.rowsToPlaylists)

      def sink = TestSink.probe[Seq[Playlist]]
      val (pub, sub) = source.via(bndStmt)
        .via(query).via(paging)
        .via(toPlaylists)
        .toMat(sink)(Keep.both).run()
      sub.request(1)
      pub.sendNext(plId)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      response should equal(playlists)
  }

  override def afterAll() {
    dropSchema(session, schema)
    close(session, cluster)
  }

}