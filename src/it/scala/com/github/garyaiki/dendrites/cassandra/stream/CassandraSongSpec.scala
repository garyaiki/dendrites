/**
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
package com.github.garyaiki.dendrites.cassandra.stream

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.Row
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.immutable.Iterable
import com.github.garyaiki.dendrites.cassandra.{PlaylistSongConfig, Songs}
import com.github.garyaiki.dendrites.cassandra.Playlists.Playlist
import com.github.garyaiki.dendrites.cassandra.Songs.Song
import com.github.garyaiki.dendrites.cassandra.fixtures.BeforeAfterAllBuilder

class CassandraSongSpec extends WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAfterAllBuilder {
  val songsTags = Set[String]("jazz", "2013")
  var songId: UUID = null
  var song: Song = null
  var songs: Seq[Song] = null
  var songIds: Seq[UUID] = null

  override def beforeAll() {
    createClusterSchemaSession(PlaylistSongConfig, 3)
    val songTRS = Songs.createTable(session, schema)
    songId = UUID.randomUUID()
    song = Song(songId,"La Petite Tonkinoise","Bye Bye Blackbird","Joséphine Baker",songsTags)
    songs = Seq(song)
    songIds = Seq(songId)
  }

  "A Cassandra Song client" should {
    "insert Songs " in {
      val iter = Iterable(songs.toSeq:_*)
      val source = Source[Song](iter)
      val bndStmt = new CassandraBind(Songs.prepInsert(session, schema), Songs.bndInsert)
      val sink = new CassandraSink(session)
      source.via(bndStmt).runWith(sink)
    }
  }

  "query a Song" in {
      val source = TestSource.probe[UUID]
      val bndStmt = new CassandraBind(Songs.prepQuery(session, schema), Songs.bndQuery)
      val query = new CassandraQuery(session)
      val paging = new CassandraPaging(10)
      def toSongs: Flow[Seq[Row], Seq[Song], NotUsed] = Flow[Seq[Row]].map(Songs.mapRows)

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

      response shouldBe songs
  }

  override def afterAll() { dropSchemaCloseSessionCluster() }
}
