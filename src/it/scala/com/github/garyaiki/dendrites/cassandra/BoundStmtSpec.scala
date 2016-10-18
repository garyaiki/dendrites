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
package com.github.garyaiki.dendrites.cassandra

import akka.actor.ActorSystem
import com.datastax.driver.core.{BoundStatement, Cluster, PreparedStatement, ResultSet, Session}
import com.datastax.driver.core.policies.{DefaultRetryPolicy, LoggingRetryPolicy, RetryPolicy}
import java.util.{HashSet => JHashSet, UUID}
import scala.concurrent.ExecutionContext
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import com.github.garyaiki.dendrites.cassandra.Playlists._
import com.github.garyaiki.dendrites.cassandra.Songs._

class BoundStmtSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val ec: ExecutionContext = system.dispatcher
  val myConfig = PlaylistSongConfig
  val schema = myConfig.keySpace
  var cluster: Cluster = null
  var session: Session = null
  val songsTags = Set[String]("jazz", "2013")
  var songId: UUID = null
  var song: Song = null
  var plId: UUID = null
  var playlist: Playlist = null

  override def beforeAll() {
    val addresses = myConfig.getInetAddresses()
    val retryPolicy = new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE)
    cluster = createCluster(addresses, retryPolicy)
    val metaData = cluster.getMetadata//DEBUG
    val lbp = createLoadBalancingPolicy(myConfig.localDataCenter)
    initLoadBalancingPolicy(cluster, lbp)
    logMetadata(cluster)
    registerQueryLogger(cluster)
    session = connect(cluster)
    val strategy = myConfig.replicationStrategy
    val createSchemaRS = createSchema(session, schema, strategy, 3)
    songId = UUID.randomUUID()
    song = Song(songId,"La Petite Tonkinoise","Bye Bye Blackbird","Joséphine Baker",songsTags)
    plId = UUID.randomUUID()
    playlist = Playlist(plId,"La Petite Tonkinoise","Bye Bye Blackbird","Joséphine Baker",songId)
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