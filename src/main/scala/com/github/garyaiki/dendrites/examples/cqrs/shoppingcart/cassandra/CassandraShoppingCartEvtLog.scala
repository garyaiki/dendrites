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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra

import com.datastax.driver.core.{BoundStatement, PreparedStatement, ResultSet, Row, Session}
import com.datastax.driver.core.utils.UUIDs
import com.weather.scalacass.syntax._
import java.util.UUID
import scala.collection.JavaConverters._
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt

/** Song is an example in Java Driver 3.0 reference doc. This provides Scala functions to create
  * a table, an insert PreparedStatement, a Query PreparedStatement, a case class, an insert
  * BoundStatement, a Query BoundStatement, and a ScalaCass Row to case class conversion
  *
  * @see [[http://docs.datastax.com/en/latest-pdf-java-driver?permalinkv1 java-driver]]
  * @see [[https://github.com/thurstonsand/scala-cass scala-cass]]
  * @author Gary Struthers
  *
  */
object CassandraShoppingCartEvtLog {

  val table = "shopping_cart_event_log"

  /** Create ShoppingCart Event log table asynchronously. executeAsync returns a ResultSetFuture which extends
    * Guava ListenableFuture. getUninterruptibly is the preferred way to complete the future
    * @param session
    * @param schema
    * @return a ResultSet which contains the first page of Rows
    */
  def createTable(session: Session, schema: String): ResultSet = {
    val resultSetF = session.executeAsync("CREATE TABLE IF NOT EXISTS " + schema + "." + table +
      " (cartId uuid, time timeuuid, eventID uuid, owner uuid, item uuid, count int, PRIMARY KEY (cartId, time));")
    resultSetF.getUninterruptibly()
  }

  /** Tell DB to prepare insert ShoppingCart Event statement. Do this once.
    *
    * @param session
    * @param schema
    * @return prepared statement
    */
  def prepInsert(session: Session, schema: String): PreparedStatement = {
    session.prepare("INSERT INTO " + schema + "." + table +
      " (cartId, time, eventID, owner, item, count) VALUES (?,?,?,?,?,?);")
  }

  /** Tell DB to prepare a query by id ShoppingCart statement. Do this once.
    *
    * @param session
    * @param schema
    * @return prepared statement
    */
  def prepQuery(session: Session, schema: String): PreparedStatement = {
    session.prepare("SELECT * FROM " + schema + "." + table + " WHERE cartId=? AND time >= ?;")
  }

  /** Bind insert PreparedStatement to values of a case class. Does not execute.
    *
    * @param insert PreparedStatement
    * @param playlst case class
    * @return BoundStatement ready to execute
    */
  def bndInsert(insert: PreparedStatement, sc: ShoppingCartEvt): BoundStatement = {
    val scBndStmt = new BoundStatement(insert)
    val count = sc.count match {
      case Some(x) => Int.box(x)
      case None => null
    }
    scBndStmt.bind(sc.cartId, sc.time, sc.eventID, sc.owner.orNull, sc.item.orNull, count)
  }

  /** Bind query by id PreparedStatement to values of a case class. Does not execute.
    *
    * @param query PreparedStatement
    * @param playlst case class
    * @return BoundStatement ready to execute
    */
  def bndQuery(query: PreparedStatement, compositeKey: (UUID, UUID)): BoundStatement = {
    val scBndStmt = new BoundStatement(query)
    val cartId = compositeKey._1
    val time = compositeKey._2
    scBndStmt.bind(cartId, time)
  }

  /** Map Row to case class. Uses ScalaCass object mapping
    * @note ScalaIDE shows an error because it doesn't find a ScalaCass implicit but SBT/Maven compiles and runs OK.
    *
    * @param row
    * @return case class
    */
  def mapRow(row: Row): ShoppingCartEvt = row.as[ShoppingCartEvt]

  def mapRows(rows: Seq[Row]): Seq[ShoppingCartEvt] = rows.map { x => mapRow(x) }
}
