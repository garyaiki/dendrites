package org.gs.examples.cassandra.boundstmtclient

import com.datastax.driver.core.{BoundStatement, Host, Row}
import java.net.InetAddress
import java.util.{UUID, HashSet => JHashSet}
import org.scalatest.WordSpecLike
import org.scalatest._
import org.scalatest.Matchers._
import scala.collection.JavaConverters._
import org.gs.examples.cassandra.boundstmtclient.fixtures.BoundStmtClientFixture
import java.util.UUID
import java.util.{HashSet => JHashSet}

class BoundStmtClientSpec extends WordSpecLike with BoundStmtClientFixture {

  val keySpaceName = " simplex"
  val replication = 3
  val songsTable = "songs"
  val songsColumns = "id, title, album, artist, tags"
  val songsStmt = s"INSERT INTO $keySpaceName.$songsTable ($songsColumns) VALUES (?, ?, ?, ?, ?);"
  val songsPrepareStmt = session.prepare(songsStmt)
  val songsId = "756716f7-2e54-4715-9f00-91dcbea6cf50"
  val songsPreBoundStmt = new BoundStatement(songsPrepareStmt)
  val jtags = new JHashSet[String]()
      jtags.add("jazz")
      jtags.add("2013")
  val songsBoundStmt = songsPreBoundStmt.bind(UUID.fromString(songsId),
      "'La Petite Tonkinoise'",
      "'Bye Bye Blackbird'",
      "'Joséphine Baker'",
      jtags)
  val playlistsTable = "playlists"
  val playlistsColumns = "id, song_id, title, album, artist"
  val playlistsStmt = s"INSERT INTO $keySpaceName.playlistsTable (playlistsColumns) VALUES (?, ?, ?, ?, ?);"
  val playlistsPrepareStmt = session.prepare(playlistsStmt)
  val playlistsId = "2cc9ccb7-6221-4ccb-8387-f22b6a1b354d"
  val playlistsPreBoundStmt = new BoundStatement(playlistsPrepareStmt)
  val playlistsBoundStmt = playlistsPreBoundStmt.bind(UUID.fromString(playlistsId),
      songsId,
      "'La Petite Tonkinoise'",
      "'Bye Bye Blackbird'",
      "'Joséphine Baker'")
  val playlistsWhere = "sid = $playlistsId"
  
  "A BoundStmtClient" should {
    "connect to a Cassandra node" in {
      assert(client.metadata.getClusterName() === "xerxes")
      val hosts = client.metadata.getAllHosts()
      val scalaHosts = hosts.asScala
      def testHosts(host: Host): Unit = {
        assert(host.getAddress() === InetAddress.getLocalHost)
        assert(host.getCassandraVersion === 3.0)
        assert(host.getRack() === "")
        assert(host.getState() === "")
      }
      scalaHosts.foreach(testHosts)
    }
    "create a keyspace" in {
      val resultSet = client.createKeySpace(session, keySpaceName, replication)
      assert(session.getLoggedKeyspace() === keySpaceName)
    }
    "create songs table" in {
      val resultSet = client.createTable(session, keySpaceName, songsTable, songsColumns)
      //assert( === )
    }
    "create playlists table" in {
      val resultSet = client.createTable(session, keySpaceName, playlistsTable, playlistsColumns)
      //assert( === )
    }
    "load songs table" in {
      val resultSet = client.loadData(songsBoundStmt)
      //assert( === )
    }
    "load playlists table" in {
      val resultSet = client.loadData(playlistsBoundStmt)
      //assert( === )
    }
    "query playlists table" in {
      val resultSet = client.querySchema("*", keySpaceName, playlistsTable, playlistsWhere)
      val scalaResultSet = resultSet.asScala
      def testRows(row: Row): Unit = {
        assert(row.getString("title") === "")
        assert(row.getString("album") === "")
        assert(row.getString("artist") === "")
      }
      scalaResultSet.foreach(testRows)
    }
  }
}