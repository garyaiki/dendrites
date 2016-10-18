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

import com.datastax.driver.core.{BoundStatement, PreparedStatement, ResultSet, Row, Session}
import com.weather.scalacass._
import com.weather.scalacass.ScalaCass._
import java.util.{HashSet => JHashSet, UUID}
import scala.collection.JavaConverters._

/** Song is an example in Java Driver 3.0 reference doc. This provides Scala functions to create
  * a table, an insert PreparedStatement, a Query PreparedStatement, a case class, an insert
  * BoundStatement, a Query BoundStatement, and a ScalaCass Row to case class conversion
  *
  * @see [[http://docs.datastax.com/en/latest-pdf-java-driver?permalinkv1 java-driver]]
  * @see [[https://github.com/thurstonsand/scala-cass scala-cass]]
  * @author Gary Struthers
  *
  */
object Songs {

  val table = "songs"

  /** Create Song table asynchronously. executeAsync returns a ResultSetFuture which extends
    * Guava ListenableFuture. getUninterruptibly is the preferred way to complete the future
    * @param session
    * @param schema
    * @return a ResultSet which contains the first page of Rows
    */
  def createTable(session: Session, schema: String): ResultSet = {
    val resultSetF = session.executeAsync("CREATE TABLE IF NOT EXISTS " + schema + "." + table +
      " (" +
      "id uuid PRIMARY KEY," +
      "title text," +
      "album text," +
      "artist text," +
      "tags set<text>," +
      "data blob" +
      ");")
    resultSetF.getUninterruptibly()
  }

  /** Tell DB to prepare insert Song statement. Do this once.
    *
    * @param session
    * @param schema
    * @return prepared statement
    */
  def songsPrepInsert(session: Session, schema: String): PreparedStatement = {
    session.prepare("INSERT INTO " + schema + "." + table +
          " (id, title, album, artist, tags) " +
          "VALUES (?,?,?,?,?);")    
  }

  /** Tell DB to prepare a query by id Song statement. Do this once.
    *
    * @param session
    * @param schema
    * @return prepared statement
    */ 
  def songsPrepQuery(session: Session, schema: String): PreparedStatement = {
      session.prepare("SELECT * FROM " + schema + "." + table + " WHERE id=?;")
  }

  case class Song(id: UUID, title: String, album: String, artist: String, tags: Set[String])

  /** Bind insert PreparedStatement to values of a case class. Does not execute. Song.tags is copied
    * from Scala Set to Java HashSet in order to bind. 
    *
    * @param insert PreparedStatement
    * @param playlst case class
    * @return BoundStatement ready to execute
    */
  def songToBndInsert(insert: PreparedStatement, song: Song): BoundStatement = {
    val songsBndStmt = new BoundStatement(insert)
    val songsTags = new JHashSet[String]()
    val it = song.tags.iterator
    while(it.hasNext) {
      songsTags.add(it.next())
    }
    songsBndStmt.bind(song.id,song.title,song.album,song.artist,songsTags)
  }

  /** Bind query by id PreparedStatement to values of a case class. Does not execute.
    *
    * @param query PreparedStatement
    * @param playlst case class
    * @return BoundStatement ready to execute
    */
  def songToBndQuery(query: PreparedStatement, songId: UUID): BoundStatement = {
    val songBndStmt = new BoundStatement(query)
    songBndStmt.bind(songId)
  }

  /** Map Row to case class. Uses ScalaCass object mapping, @FIXME Eclipse reports an error because
    * it doesn't find a ScalaCass implicit but SBT compiles and runs this.
    *
    * @param row
    * @return case class
    */
  def rowToSong(row: Row): Song = {
    row.as[Song]
  }

  def rowsToSongs(rows: Seq[Row]): Seq[Song] = {
    rows.map { x => rowToSong(x) }
  }
}
