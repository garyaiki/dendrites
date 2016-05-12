package org.gs.cassandra.stream

import com.datastax.driver.core.{BoundStatement, Cluster, PreparedStatement, ResultSet, Session}
import com.datastax.driver.core.policies.{DefaultRetryPolicy, LoggingRetryPolicy, RetryPolicy}
import java.util.{HashSet => JHashSet, UUID}
import org.scalatest._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import org.gs.cassandra._
import org.gs.cassandra.Playlists._
import org.gs.cassandra.Songs._

class CassandraSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  val myConfig = PlaylistSongConfig
  val schema = myConfig.keySpace
  var cluster: Cluster = null
  var session: Session = null
  val songsTags = Set[String]("jazz", "2013")
  val songId = UUID.fromString("756716f7-2e54-4715-9f00-91dcbea6cf50")
  val song = Song(songId,"La Petite Tonkinoise","Bye Bye Blackbird","Joséphine Baker",songsTags)
  val plId = UUID.fromString("2cc9ccb7-6221-4ccb-8387-f22b6a1b354d")
  val playlist = Playlist(plId,"La Petite Tonkinoise","Bye Bye Blackbird","Joséphine Baker",songId)

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
  }

  "A Cassandra client" should {
    "create a Song table" in {
      val songTRS = Songs.createTable(session, schema)
      songTRS shouldBe a [ResultSet]
    }

    "create a Playlist table" in {
      val playlistTRS = Playlists.createTable(session, schema)
      playlistTRS shouldBe a [ResultSet]
    }
  }

  "insert a Song" in {
      val prepStmt = Songs.songsPrepInsert(session, schema)
      val bndStmt = songToBndInsert(prepStmt, song)
      val rs = executeBoundStmt(session, bndStmt)
      rs shouldBe a [ResultSet]
  }

  "insert a Playlist" in {
      val prepStmt = Playlists.playlistsPrepInsert(session, schema)
      val bndStmt = playlistToBndInsert(prepStmt, playlist)
      val rs = executeBoundStmt(session, bndStmt)
      rs shouldBe a [ResultSet]
  }

  "query a Song" in {
      val prepStmt = Songs.songsPrepQuery(session, schema)
      val bndStmt = songToBndQuery(prepStmt, song.id)
      val rs = executeBoundStmt(session, bndStmt)
      val row = rs.one()
      assert(rowToSong(row) === song)
  }

  "query a Playlist" in {
      val prepStmt = Playlists.playlistsPrepQuery(session, schema)
      val bndStmt = playlistToBndQuery(prepStmt, plId)
      val rs = executeBoundStmt(session, bndStmt)
      val row = rs.one()
      assert(rowToPlaylist(row) === playlist)
  }

  override def afterAll() {
    dropSchema(session, schema)
    close(session, cluster)
  }

}