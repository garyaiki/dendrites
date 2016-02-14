package org.gs.examples.cassandra.querybldrclient

import com.datastax.driver.core.{BoundStatement, Host, Row}
import com.datastax.driver.core.querybuilder.QueryBuilder
import java.net.InetAddress
import java.util.{UUID, HashSet => JHashSet}
import org.scalatest.WordSpecLike
import org.scalatest._
import org.scalatest.Matchers._
import scala.collection.JavaConverters._
import org.gs.examples.cassandra.querybldrclient.fixtures.QueryBldrClientFixture
import java.util.{HashSet => JHashSet}
import scala.IndexedSeq

class QueryBldrClientSpec extends WordSpecLike with QueryBldrClientFixture {

  val keyspace = " simplex"
  val replication = 3
  val songsTable = "songs"
  val songsColumns = "id, title, album, artist, tags"
  val songsStmt = s"INSERT INTO $keyspace.$songsTable ($songsColumns) VALUES (?, ?, ?, ?, ?);"
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
  val songsSelectAll = QueryBuilder.select().all().from(keyspace, songsTable)
  val playlistsTable = "playlists"
  val playlistsColumns = "id, song_id, title, album, artist"
  val playlistsStmt = s"INSERT INTO $keyspace.playlistsTable (playlistsColumns) VALUES (?, ?, ?, ?, ?);"
  val playlistsPrepareStmt = session.prepare(playlistsStmt)
  val playlistsId = "2cc9ccb7-6221-4ccb-8387-f22b6a1b354d"
  val playlistsPreBoundStmt = new BoundStatement(playlistsPrepareStmt)
  val playlistsBoundStmt = playlistsPreBoundStmt.bind(UUID.fromString(playlistsId),
      songsId,
      "'La Petite Tonkinoise'",
      "'Bye Bye Blackbird'",
      "'Joséphine Baker'")
  val playlistsWhere = "sid = $playlistsId"
  val addressBookKeySpace = "addressbook"
  val contactTable = "contact"
  val contactColumns = IndexedSeq[String]("firstName", "lastName", "email")
  val contactInsert = QueryBuilder.insertInto(addressBookKeySpace, contactTable)
      .value(contactColumns(0), "Dwayne")
      .value(contactColumns(1), "Garcia")
      .value(contactColumns(2), "dwayne@example.com")
  val contactClause = QueryBuilder.eq("username", "dgarcia")
  val contactUpdate = QueryBuilder.update(addressBookKeySpace, contactTable)
  contactUpdate.`with`(QueryBuilder.add(contactColumns(2), "dwayne@example.com"))
  contactUpdate.where(contactClause)
  val contactDelete = QueryBuilder.delete()
      .from(addressBookKeySpace, contactTable)
  val contactDeleteWhere = contactDelete.where(contactClause)

  "A QueryBldrClientClient" should {
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
      val resultSet = client.createKeySpace(session, keyspace, replication)
      assert(session.getLoggedKeyspace() === keyspace)
    }
    "create songs table" in {
      val resultSet = client.createTable(session, keyspace, songsTable, songsColumns)
      //assert( === )
    }
    "create playlists table" in {
      val resultSet = client.createTable(session, keyspace, playlistsTable, playlistsColumns)
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
      val resultSet = client.querySchema("*", keyspace, playlistsTable, playlistsWhere)
      val scalaResultSet = resultSet.asScala
      def testRows(row: Row): Unit = {
        assert(row.getString("title") === "")
        assert(row.getString("album") === "")
        assert(row.getString("artist") === "")
      }
      scalaResultSet.foreach(testRows)
    }

    "insert contact row" in {
      val rows = client.insert(contactInsert)
      val it = rows.iterator()
      while(it.hasNext()) {
        it.next()
      }
      // assert()
    }

    "select songs rows" in {
      val rows = client.select(songsSelectAll)
      assert(rows.size() === 1) 
    }

    "update contact row" in {
      client.update(contactUpdate)
    }

    "delete contact row" in {
      client.deleteWhere(contactDeleteWhere)
    }
  }
}