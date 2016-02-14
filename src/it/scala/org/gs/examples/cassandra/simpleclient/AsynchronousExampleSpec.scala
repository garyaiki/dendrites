package org.gs.examples.cassandra.simpleclient

import com.datastax.driver.core.Host
import java.net.InetAddress
import org.scalatest.WordSpecLike
import org.scalatest._
import org.scalatest.Matchers._
import scala.collection.JavaConverters._
import org.gs.examples.cassandra.simpleclient.fixtures.AsynchronousExampleFixture

class AsynchronousExampleSpec extends WordSpecLike with AsynchronousExampleFixture {

  val keySpaceName = " simplex"
  val replication = 3
  val songsTable = "songs"
  val songsColumns = """id uuid PRIMARY KEY,
    title text,
    album text,
    artist text,
    tags set<text>,
    data blob"""
  val songsColumnNames = "id, title, album, artist, tags"
  val songsValues = """756716f7-2e54-4715-9f00-91dcbea6cf50,
        'La Petite Tonkinoise',
        'Bye Bye Blackbird',
        'Joséphine Baker',
        {'jazz', '2013'}"""
  val playlistsTable = "playlists"
  val playlistsColumns = """id uuid,
    title text,
    album text,
    artist text,
    song_id uuid,
    PRIMARY KEY (id, title, album, artist)"""
  val playlistsColumnNames = "id, title, album, artist"
  val playlistsValues = """2cc9ccb7-6221-4ccb-8387-f22b6a1b354d,
          756716f7-2e54-4715-9f00-91dcbea6cf50,
          'La Petite Tonkinoise',
          'Bye Bye Blackbird',
          'Joséphine Baker'"""
  val playlistsWhere = "id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d"
  
  "A AsynchronousExample" should {
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
      val resultSet = client.loadData(keySpaceName, songsTable, songsColumnNames, songsValues)
      //assert( === )
    }
    "load playlists table" in {
      val resultSet = client.loadData(keySpaceName, playlistsTable, playlistsColumnNames, playlistsValues)
      //assert( === )
    }
    "get playlists rows" in {
      val results = client.getRows(keySpaceName, playlistsTable)
      val r = results.getUninterruptibly()

      /*
			for (Row row : results.getUninterruptibly()) {
         System.out.printf( "%s: %s / %s\n",
               row.getString("artist"),
               row.getString("title"),
               row.getString("album") );
      }
      */
    }
  }
}