package org.gs.cassandra.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{BoundStatement,Cluster,PreparedStatement,ResultSet,Row,Session}
import com.datastax.driver.core.policies.{DefaultRetryPolicy, LoggingRetryPolicy, RetryPolicy}
import java.util.{HashSet => JHashSet, UUID}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.immutable.Iterable
import scala.concurrent.ExecutionContext.Implicits.global
import org.gs.cassandra.{Playlists, PlaylistSongConfig}
import org.gs.cassandra.Playlists._
import org.gs.cassandra.Playlists._
import org.gs.cassandra.{close, connect, createCluster, createLoadBalancingPolicy, createSchema}
import org.gs.cassandra.{dropSchema, initLoadBalancingPolicy, logMetadata, registerQueryLogger}

class CassandraPlaylistSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val myConfig = PlaylistSongConfig
  val schema = myConfig.keySpace
  var cluster: Cluster = null
  var session: Session = null
  var songId: UUID = null// = UUID.randomUUID()//.fromString("756716f7-2e54-4715-9f00-91dcbea6cf50")
  var plId: UUID = null// = UUID.randomUUID()//.fromString("2cc9ccb7-6221-4ccb-8387-f22b6a1b354d")
  var playlist: Playlist = null// = Playlist(plId,"La Petite Tonkinoise","Bye Bye Blackbird","Joséphine Baker",songId)
  var playlists: Seq[Playlist] = null// = Seq(playlist)
  var playlistIds: Seq[UUID] = null// = Seq(plId)

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
    songId = UUID.randomUUID()//.fromString("756716f7-2e54-4715-9f00-91dcbea6cf50")
    plId = UUID.randomUUID()//.fromString("2cc9ccb7-6221-4ccb-8387-f22b6a1b354d")
    playlist = Playlist(plId,"La Petite Tonkinoise","Bye Bye Blackbird","Joséphine Baker",songId)
    playlists = Seq(playlist)
    playlistIds = Seq(plId)
    val songTRS = Playlists.createTable(session, schema)
  }

  "A Cassandra Playlist client" should {
    "insert Playlists " in {
      val iter = Iterable(playlists.toSeq:_*)
      val source = Source[Playlist](iter)
      val bndStmt = new CassandraBind(Playlists.playlistsPrepInsert(session, schema),
              playlistToBndInsert)
      val sink = new CassandraSink(session)
      source.via(bndStmt).runWith(sink)
    }
  }

  "query a Playlist" in {
      val source = TestSource.probe[UUID]
      val bndStmt = new CassandraBind(Playlists.playlistsPrepQuery(session, schema),
              playlistToBndQuery)
      val query = new CassandraQuery(session)
      val paging = new CassandraPaging(10)
      def toPlaylists: Flow[Seq[Row], Seq[Playlist], NotUsed] =
            Flow[Seq[Row]].map(Playlists.rowsToPlaylists)

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