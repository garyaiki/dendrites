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
import akka.event.Logging
import com.datastax.driver.core.{BoundStatement, PreparedStatement, Session}
import com.typesafe.config.ConfigFactory
import java.util.{HashSet => JHashSet, UUID}

class BoundStatementClient() {/*
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  val songId = UUID.fromString("756716f7-2e54-4715-9f00-91dcbea6cf50")
  val playlistId = UUID.fromString("2cc9ccb7-6221-4ccb-8387-f22b6a1b354d")

  def createSchema(session: Session, schema: String): Unit = {
    val rsf = session.executeAsync("CREATE KEYSPACE IF NOT EXISTS " + schema + " WITH replication"
        + "= {'class': 'SimpleStrategy', 'replication_factor':3};")
    rsf.getUninterruptibly() // blocks, schema can't be used before completes 
  }

  def loadData(session: Session,
               schema: String,
               songsStmt: PreparedStatement,
               playlistsStmt: PreparedStatement): Unit = {

    val songsTags = new JHashSet[String]()
    songsTags.add("jazz")
    songsTags.add("2013")
    val songsBndStmt = new BoundStatement(songsStmt)
    val bnd = songsBndStmt.bind(songId,
              "'La Petite Tonkinoise'",
              "'Bye Bye Blackbird'",
              "'Joséphine Baker'",
              songsTags)
    val rsf1 = session.executeAsync(bnd)

    val playListsBndStmt = new BoundStatement(playlistsStmt)
    val rsf2 = session.executeAsync(playListsBndStmt.bind(playlistId,
            "'La Petite Tonkinoise'",
            "'Bye Bye Blackbird'",
            "'Joséphine Baker'",
            songId))

    rsf1.getUninterruptibly()
    rsf2.getUninterruptibly()
  }

  def querySchema(session: Session, schema: String, qPlaylists: PreparedStatement): Unit = {

    val qPLBndStmt = new BoundStatement(qPlaylists)
    val rsf = session.executeAsync(qPLBndStmt.bind(playlistId))
    val results = rsf.getUninterruptibly()
    val it = results.iterator()
    while(it.hasNext()) {
      val row = it.next()
      logger.debug(s"""title:${row.getString("title")}
        album:${row.getString("album")},
        artist:${row.getString("artist")}""") 
    }
  }
  
  def updateSchema(): Unit = {
    logger.debug("updateSchema")
  }*/
}

object BoundStatementClient {/*
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val ipAddress = config.getString("dendrites.cassandra.ipAddress")
    val client = new BoundStatementClient()
    val cluster = createCluster(ipAddress)
    val session = connect(cluster)
    val schema = config.getString("dendrites.cassandra.keySpace")
    val strategy = config.getString("dendrites.cassandra.replicationStrategy")
    val createSchemaRS = createSchema(session, schema, strategy, 3)
    logger.debug("createSchemaRS {}", createSchemaRS.toString())
    val songsTRS = Songs.createTable(session, schema)
    logger.debug("songs create table {}",songsTRS.toString())
    val playlistsTRS = Playlists.createTable(session, schema)
    logger.debug("playlists create table {}",playlistsTRS.toString())
    val songsStmt = Songs.songsPrepInsert(session, schema)
    val playlistsStmt = Playlists.playlistsPrepInsert(session, schema)
    client.loadData(session, schema, songsStmt, playlistsStmt)
    val qPlaylists = Playlists.playlistsPrepQuery(session, schema)
    client.querySchema(session, schema, qPlaylists)
    val plPreStmt = selectAll(session, schema, Playlists.table)
    val selAllRS = executeBoundStmt(session, new BoundStatement(plPreStmt))
    logger.debug("playlists select all {}", selAllRS.toString())
    client.updateSchema()
    dropSchema(session, "simplex")
    close(session, cluster)
  }*/
}
