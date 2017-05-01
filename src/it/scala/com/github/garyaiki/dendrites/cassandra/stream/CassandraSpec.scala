/**
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
package com.github.garyaiki.dendrites.cassandra.stream

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{Cluster, PreparedStatement, ResultSet, Session}
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.ExecutionContext
import com.github.garyaiki.dendrites.cassandra.{Playlists, PlaylistSongConfig, Songs}
import com.github.garyaiki.dendrites.cassandra.Playlists.Playlist
import com.github.garyaiki.dendrites.cassandra.Songs.Song
import com.github.garyaiki.dendrites.cassandra.{close, connect, createSchema, dropSchema, executeBoundStmt}
import com.github.garyaiki.dendrites.cassandra.fixtures.{buildCluster, getOneRow}
import com.github.garyaiki.dendrites.stream.SpyFlow

class CassandraSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val myConfig = PlaylistSongConfig
  var cluster: Cluster = null
  val schema = myConfig.keySpace
  var session: Session = null
  val songsTags = Set[String]("jazz", "2013")
  val songId = UUID.randomUUID
  val song = Song(songId, "La Petite Tonkinoise", "Bye Bye Blackbird", "Joséphine Baker", songsTags)
  val plId = UUID.randomUUID
  val playlist = Playlist(plId, "La Petite Tonkinoise", "Bye Bye Blackbird", "Joséphine Baker", songId)
  var prepSongStmt: PreparedStatement = null

  override def beforeAll() {
    cluster = buildCluster(myConfig)
    session = connect(cluster)
    val strategy = myConfig.replicationStrategy
    val createSchemaRS = createSchema(session, schema, strategy, 3)
  }

  "A Cassandra client" should {
    "create a Song table" in {
      val songTRS = Songs.createTable(session, schema)
      songTRS shouldBe a[ResultSet]
      prepSongStmt = Songs.prepQuery(session, schema)
    }

    "create a Playlist table" in {
      val playlistTRS = Playlists.createTable(session, schema)
      playlistTRS shouldBe a[ResultSet]
    }
  }

  "insert a Song" in {
    val prepStmt = Songs.prepInsert(session, schema)
    val bndStmt = Songs.bndInsert(prepStmt, song)
    val rs = executeBoundStmt(session, bndStmt)
    rs shouldBe a[ResultSet]
  }

  "insert a Playlist" in {
    val prepStmt = Playlists.prepInsert(session, schema)
    val bndStmt = Playlists.bndInsert(prepStmt, playlist)
    val rs = executeBoundStmt(session, bndStmt)
    rs shouldBe a[ResultSet]
  }

  "query a Song" in {
    val rs = executeBoundStmt(session, Songs.bndQuery(prepSongStmt, songId))
    val row = rs.one()
    Songs.mapRow(row) shouldBe song
  }

  "query a Playlist" in {
    val prepStmt = Playlists.prepQuery(session, schema)
    val bndStmt = Playlists.bndQuery(prepStmt, plId)
    val rs = executeBoundStmt(session, bndStmt)
    val row = rs.one()
    Playlists.mapRow(row) shouldBe playlist
  }

  "query a Song with BoundQuery" in {
    val source = TestSource.probe[UUID]
    val query = new CassandraBoundQuery[UUID](session, prepSongStmt, Songs.bndQuery, 1)
    //val spy = new SpyFlow[ResultSet]("query a Song with BoundQuery spy 1", 0, 0)
    def sink = TestSink.probe[ResultSet]
    val (pub, sub) = source.via(query).toMat(sink)(Keep.both).run()
    val row = getOneRow(songId, (pub, sub))
    pub.sendComplete()
    sub.expectComplete()

    val responseShoppingCart = Songs.mapRow(row)
    responseShoppingCart shouldBe song
  }
  override def afterAll() {
    dropSchema(session, schema)
    close(session, cluster)
  }
}
