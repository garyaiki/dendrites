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
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt

/** Functions to create a table, an Insert PreparedStatement and BoundStatment, a Query PreparedStatement and
  * BoundStatement, a ScalaCass map Row to ShoppingCartEvt, and map a Seq[Row] to Seq[ShoppingCartEvt}
  *
  * @see [[http://docs.datastax.com/en/latest-pdf-java-driver?permalinkv1 java-driver]]
  * @see [[https://github.com/thurstonsand/scala-cass scala-cass]]
  * @author Gary Struthers
  *
  */
object CassandraShoppingCartEvtLog {

  val table = "shopping_cart_event_log"

  /** Create ShoppingCart Event log table asynchronously. executeAsync returns a ResultSetFuture which extends
    * Guava ListenableFuture. getUninterruptibly is Cassandra's recommended way to complete their future.
    *
    * @param session
    * @param schema
    * @return a ResultSet which contains the first page of Rows
    */
  def createTable(session: Session, schema: String): ResultSet = {
    val resultSetF = session.executeAsync("CREATE TABLE IF NOT EXISTS " + schema + "." + table +
      """ (id uuid, eventID uuid, cartId uuid, time timeuuid, owner uuid, item uuid, count int,
         PRIMARY KEY ((id, cartId), time));""")
    resultSetF.getUninterruptibly()
  }

  /** Tell DB to prepare insert ShoppingCart Event statement.
    *
    * @param session
    * @param schema
    * @return prepared statement
    */
  def prepInsert(session: Session, schema: String): PreparedStatement = {
    session.prepare("INSERT INTO " + schema + "." + table +
      " (id, eventID, cartId, time, owner, item, count) VALUES (?,?,?,?,?,?,?);")
  }

  /** Bind insert PreparedStatement to values of a case class.
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
    scBndStmt.bind(sc.id, sc.eventID, sc.cartId, sc.time, sc.owner.orNull, sc.item.orNull, count)
  }

  /** Tell DB to prepare a query by id ShoppingCart statement. Do this once.
    *
    * ALLOW FILTERING is necessary because we don't query on the key.
    * @param session
    * @param schema
    * @return prepared statement
    */
  def prepQuery(session: Session, schema: String): PreparedStatement = {
    session.prepare("SELECT * FROM " + schema + "." + table + " WHERE cartId=? AND time >= ? ALLOW FILTERING;")
  }

  /** Bind query by id PreparedStatement to values of a case class.
    *
    * @param query PreparedStatement
    * @param queryArgs cartId, time
    * @return BoundStatement ready to execute
    */
  def bndQuery(query: PreparedStatement, queryArgs: (UUID, UUID)): BoundStatement = {
    val scBndStmt = new BoundStatement(query)
    val cartId = queryArgs._1
    val time = queryArgs._2
    scBndStmt.bind(cartId, time)
  }

  /** Map Row to case class. Uses ScalaCass object mapping
    * @note ScalaIDE shows an error because it doesn't find a ScalaCass implicit but SBT/Maven compiles and runs OK.
    *
    * @param row
    * @return case class
    */
  def mapRow(row: Row): ShoppingCartEvt = row.as[ShoppingCartEvt] // Presentation compiler in Scala-ide shows error

  def mapRows(rows: Seq[Row]): Seq[ShoppingCartEvt] = rows.map { x => mapRow(x) }
}
