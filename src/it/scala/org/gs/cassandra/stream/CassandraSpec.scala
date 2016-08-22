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
package org.gs.cassandra.stream

import com.datastax.driver.core.{BoundStatement, Cluster, PreparedStatement, ResultSet, Session}
import com.datastax.driver.core.policies.{DefaultRetryPolicy,
  ExponentialReconnectionPolicy,
  LoggingRetryPolicy,
  RetryPolicy}
import java.util.{HashSet => JHashSet, UUID}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatest.Matchers._
import org.gs.cassandra.{Playlists, PlaylistSongConfig, Songs}
import org.gs.cassandra.Playlists._
import org.gs.cassandra.Songs._
import org.gs.cassandra.{close, connect, createCluster, createLoadBalancingPolicy, createSchema}
import org.gs.cassandra.{dropSchema, executeBoundStmt, initLoadBalancingPolicy, logMetadata}
import org.gs.cassandra.registerQueryLogger

class CassandraSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  val myConfig = PlaylistSongConfig
  val schema = myConfig.keySpace
  var cluster: Cluster = null
  var session: Session = null
  val songsTags = Set[String]("jazz", "2013")
  val songId = UUID.randomUUID()//.fromString("756716f7-2e54-4715-9f00-91dcbea6cf50")
  val song = Song(songId,"La Petite Tonkinoise","Bye Bye Blackbird","Joséphine Baker",songsTags)
  val plId = UUID.randomUUID()//.fromString("2cc9ccb7-6221-4ccb-8387-f22b6a1b354d")
  val playlist = Playlist(plId,"La Petite Tonkinoise","Bye Bye Blackbird","Joséphine Baker",songId)

  override def beforeAll() {
    val addresses = myConfig.getInetAddresses()
    val retryPolicy = new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE)
    val reConnectPolicy = new ExponentialReconnectionPolicy(10L, 10L)
    cluster = createCluster(addresses, retryPolicy, reConnectPolicy)
    cluster.init()//DEBUG
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

      rowToSong(row) shouldBe song
  }

  "query a Playlist" in {
      val prepStmt = Playlists.playlistsPrepQuery(session, schema)
      val bndStmt = playlistToBndQuery(prepStmt, plId)
      val rs = executeBoundStmt(session, bndStmt)
      val row = rs.one()

      rowToPlaylist(row) shouldBe playlist
  }

  override def afterAll() {
    dropSchema(session, schema)
    close(session, cluster)
  }
}
