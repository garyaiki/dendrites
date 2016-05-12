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
import org.gs.cassandra.Songs._

class CassandraSongSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val myConfig = PlaylistSongConfig
  val schema = myConfig.keySpace
  var cluster: Cluster = null
  var session: Session = null
  val songsTags = Set[String]("jazz", "2013")
  val songId = UUID.fromString("756716f7-2e54-4715-9f00-91dcbea6cf50")
  val song = Song(songId,"La Petite Tonkinoise","Bye Bye Blackbird","Joséphine Baker",songsTags)
  val songs = Seq(song)
  val songIds = Seq(songId)

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
    val songTRS = Songs.createTable(session, schema)
  }

  "A Cassandra Song client" should {
    "insert Songs " in {
      val iter = Iterable(songs.toSeq:_*)
      val source = Source[Song](iter)
      val bndStmt = new CassandraBind(Songs.songsPrepInsert(session, schema), songToBndInsert)
      val sink = new CassandraSink(session)
      source.via(bndStmt).runWith(sink)
    }
  }

  "query a Song" in {
      val source = TestSource.probe[UUID]
      val bndStmt = new CassandraBind(Songs.songsPrepQuery(session, schema), songToBndQuery)
      val query = new CassandraQuery(session)
      val paging = new CassandraPaging(10)
      def toSongs: Flow[Seq[Row], Seq[Song], NotUsed] = Flow[Seq[Row]].map(Songs.rowsToSongs)

      def sink = TestSink.probe[Seq[Song]]
      val (pub, sub) = source.via(bndStmt)
        .via(query).via(paging)
        .via(toSongs)
        .toMat(sink)(Keep.both).run()
      sub.request(1)
      pub.sendNext(songId)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      response should equal(songs)
  }

  override def afterAll() {
    dropSchema(session, schema)
    close(session, cluster)
  }

}