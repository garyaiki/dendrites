package org.gs.cassandra

import com.datastax.driver.core.{ BoundStatement, PreparedStatement, ResultSet, Row, Session }
import com.weather.scalacass._
import com.weather.scalacass.ScalaCass._
import java.util.UUID
import scala.collection.JavaConverters._

/** Playlist is an example in Java Driver 3.0 reference doc. This provides Scala functions to create
  * a table, an insert PreparedStatement, a Query PreparedStatement, a case class, an insert
  * BoundStatement, a Query BoundStatement, and a ScalaCass Row to case class conversion
  *
  * @see http://docs.datastax.com/en/latest-pdf-java-driver?permalinkv1
  * @see https://github.com/thurstonsand/scala-cass
  * @author Gary Struthers
  *
  */
object Playlists {

  val table = "playlists"

  /** Create Playlist table asychronously. executeAsync returns a ResultSetFuture which extends
    * Guava ListenableFuture. getUninterruptibly is the preferred way to complete the future
    * @param session
    * @param schema
    * @return a ResultSet which contains the first page of Rows
    */
  def createTable(session: Session, schema: String): ResultSet = {
    val resultSetF = session.executeAsync("CREATE TABLE IF NOT EXISTS " + schema + "." + table +
        " (id uuid," +
        "title text," +
        "album text," +
        "artist text," +
        "song_id uuid," +
        "PRIMARY KEY (id, title, album, artist));")
    resultSetF.getUninterruptibly()
  }

  /** Tell Cassandra DB to prepare insert Playlist statement. Do this once.
    *
    * @param session
    * @param schema
    * @return prepared statement
    */
  def playlistsPrepInsert(session: Session, schema: String): PreparedStatement = {
    session.prepare("INSERT INTO " + schema + "." + table +
        " (id, title, album, artist, song_id) " +
        "VALUES (?,?,?,?,?);")
  }

  /** Tell Cassandra DB to prepare a query by id Playlist statement. Do this once.
    *
    * @param session
    * @param schema
    * @return prepared statement
    */  
  def playlistsPrepQuery(session: Session, schema: String): PreparedStatement = {
      session.prepare("SELECT * FROM " + schema + "." + table + " WHERE id=?;")
  }

  case class Playlist(id: UUID, title: String, album: String, artist: String, songId: UUID)

  /** Bind insert PreparedStatement to values of a case class. Does not execute.
    *
    * @param insert PreparedStatement
    * @param playlst case class
    * @return BoundStatement ready to execute
    */
  def playlistToBndInsert(insert: PreparedStatement, playlst: Playlist): BoundStatement = {
    val playlistBndStmt = new BoundStatement(insert)
    playlistBndStmt.bind(playlst.id, playlst.title, playlst.album, playlst.artist, playlst.songId)
  }

  /** Bind query by id PreparedStatement to values of a case class. Does not execute.
    *
    * @param query PreparedStatement
    * @param playlst case class
    * @return BoundStatement ready to execute
    */
  def playlistToBndQuery(query: PreparedStatement, playlst: Playlist): BoundStatement = {
    val playlistBndStmt = new BoundStatement(query)
    playlistBndStmt.bind(playlst.id)
  }

  /** Map Row to case class. Uses ScalaCass field mapping because song_id in db doesn't match name
    * of songId in case class
    *
    * @param row
    * @return case class
    */
  def rowToPlaylist(row: Row): Playlist = {
    Playlist(row.as[UUID]("id"),
             row.as[String]("title"),
             row.as[String]("album"),
             row.as[String]("artist"),
             row.as[UUID]("song_id"))
  }
}