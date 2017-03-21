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

import akka.event.LoggingAdapter
import com.datastax.driver.core.{BoundStatement, PreparedStatement, ResultSet, ResultSetFuture, Row, Session}
import com.datastax.driver.core.exceptions.InvalidQueryException
import com.weather.scalacass.syntax._
import java.util.UUID
import scala.collection.JavaConverters._
import scala.collection.mutable.{ArrayBuffer, StringBuilder}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.{ShoppingCart, SetItems, SetOwner}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd

/** This provides Scala functions to create
  * a table, an insert PreparedStatement, a Query PreparedStatement, a case class, an insert
  * BoundStatement, a Query BoundStatement, and a ScalaCass Row to case class conversion
  *
  * @see [[http://docs.datastax.com/en/latest-pdf-java-driver?permalinkv1 java-driver]]
  * @see [[https://github.com/thurstonsand/scala-cass scala-cass]]
  * @author Gary Struthers
  *
  */
object CassandraShoppingCart {

  val table = "shopping_cart"

  /** Create ShoppingCart table asynchronously. executeAsync returns a ResultSetFuture which extends
    * Guava ListenableFuture. getUninterruptibly is the preferred way to complete the future
    *
    * @param session
    * @param schema
    * @return a ResultSet which contains the first page of Rows
    */
  def createTable(session: Session, schema: String): ResultSet = {
    val resultSetF = session.executeAsync("CREATE TABLE IF NOT EXISTS " + schema + "." + table +
      " (cartId uuid,owner uuid,items map<uuid, int>,version int,PRIMARY KEY (cartId));")
    resultSetF.getUninterruptibly()
  }

  /** Tell DB to prepare insert ShoppingCart statement. Do this once.
    *
    * @param session
    * @param schema
    * @return prepared statement
    */
  def prepInsert(session: Session, schema: String): PreparedStatement = {
    session.prepare("INSERT INTO " + schema + "." + table + " (cartId, owner, items, version) VALUES (?,?,?,?);")
  }

  /** Bind insert ShoppingCart PreparedStatement to values of ShoppingCart.
    *
    * @param stmt PreparedStatement
    * @param cc ShoppingCart
    * @return BoundStatement ready to execute
    */
  def bndInsert(stmt: PreparedStatement, cc: ShoppingCart): BoundStatement = {
    val scBndStmt = new BoundStatement(stmt)
    scBndStmt.bind(cc.cartId, cc.owner, cc.items.asJava, Int.box(cc.version))
  }

  /** Tell DB to prepare update ShoppingCart owner statement. Optimistic locking, version must be current
    *
    * @param session
    * @param schema
    * @return prepared statement
    */
  def prepUpdateOwner(session: Session, schema: String): PreparedStatement = {
    session.prepare("UPDATE " + schema + "." + table + " SET owner = ?, version = ? WHERE cartId = ? IF version = ?");
  }

  /** Bind update PreparedStatement to owner value of SetOwner.
    *
    * @param stmt PreparedStatement
    * @param cc SetOwner
    * @return BoundStatement ready to execute
    */
  def bndUpdateOwner(stmt: PreparedStatement, cc: SetOwner): BoundStatement = {
    val scBndStmt = new BoundStatement(stmt)
    scBndStmt.bind(cc.owner, Int.box(cc.version + 1), cc.cartId, Int.box(cc.version))
  }

  /** Tell DB to prepare update ShoppingCart items statement. Optimistic locking, version must be current
    *
    * @param session
    * @param schema
    * @return prepared statement
    */
  def prepUpdateItems(session: Session, schema: String): PreparedStatement = {
    session.prepare("UPDATE " + schema + "." + table + " SET items = ?, version = ? WHERE cartId = ? IF version = ?");
  }

  /** Bind update ShoppingCart PreparedStatement to items values of SetItems.
    *
    * @param stmt PreparedStatement
    * @param cc SetItems
    * @return BoundStatement ready to execute
    */
  def bndUpdateItems(stmt: PreparedStatement, cc: SetItems): BoundStatement = {
    val scBndStmt = new BoundStatement(stmt)
    scBndStmt.bind(cc.items.asJava, Int.box(cc.version + 1), cc.cartId, Int.box(cc.version))
  }

  /** Tell DB to prepare a query by id ShoppingCart statement.
    *
    * @param session
    * @param schema
    * @return prepared statement
    */
  def prepQuery(session: Session, schema: String): PreparedStatement = {
    session.prepare("SELECT * FROM " + schema + "." + table + " WHERE cartId=?;")
  }

  /** Bind query by id PreparedStatement to values of a case class.
    *
    * @param query PreparedStatement
    * @param cartId
    * @return BoundStatement ready to execute
    */
  def bndQuery(query: PreparedStatement, cartId: UUID): BoundStatement = {
    val scBndStmt = new BoundStatement(query)
    scBndStmt.bind(cartId)
  }

  /** Tell DB to prepare delete ShoppingCart row.
    *
    * @param session
    * @param schema
    * @return prepared statement
    */
  def prepDelete(session: Session, schema: String): PreparedStatement = {
    session.prepare("DELETE FROM " + schema + "." + table + " WHERE cartId = ? ");
  }

  /** Bind update PreparedStatement to owner value of SetOwner.
    *
    * @param stmt delete row PreparedStatement
    * @param cartId
    * @return BoundStatement ready to execute
    */
  def bndDelete(stmt: PreparedStatement, cartId: UUID): BoundStatement = {
    val scBndStmt = new BoundStatement(stmt)
    scBndStmt.bind(cartId: UUID)
  }

  /** Log ShoppingCart row elements returned by a conditional update or insert
    *
    * @param row
    * @param log
    */
  def rowAction(row: Row)(implicit log: LoggingAdapter): Unit = {
    val buf = new ArrayBuffer[String]
    val colDefs = row.getColumnDefinitions.asList.asScala
    colDefs foreach (x => buf.+=(x.getName))
    val sb = new StringBuilder("ShoppingCart rowAction error ")
    def rowToString(name: String): Unit = name match {
      case "[applied]" =>
        sb.append(name); sb.append(':'); sb.append(row.getBool("[applied]")); sb.append(' ')
      case "version" =>
        sb.append(name); sb.append(':'); sb.append(row.getInt("version")); sb.append(' ')
      case "cartId" =>
        sb.append(name); sb.append(':'); sb.append(row.getUUID("cartId")); sb.append(' ')
      case "items" => {
        sb.append(name)
        sb.append(':')
        val map = row.getMap("items", Class.forName("UUID"), Class.forName("Int")).asScala
        map.foreach { kv =>
          {
            sb.append("key:")
            sb.append(kv._1)
            sb.append(" value:")
            sb.append(kv._2)
          }
          sb.append(' ')
        }
      }
      case "owner" => sb.append(name); sb.append(':'); sb.append(row.getUUID("owner")); sb.append(' ')
    }
    buf foreach rowToString
    log.warning(sb.toString)
  }

  /** Query ShoppingCard table by id map to ShoppingCart case class
    *
    * @param session
    * @param queryStmt: PreparedStatement
    * @param cartId: UUID
    * @return ShoppingCart
    */
  def getShoppingCart(session: Session, queryStmt: PreparedStatement, cartId: UUID): Option[ShoppingCart] = {
    val boundQuery = bndQuery(queryStmt, cartId)
    val queryRS = session.execute(boundQuery)
    val row = queryRS.one
    if (row == null) None else Some(mapRow(row))
  }

  /** Update ShoppingCart owner, asynchronous
    *
    * @param session
    * @param setOwnerStmt
    * @param setOwner
    * @return ResultSet
    */
  def updateOwner(session: Session, setOwnerStmt: PreparedStatement, setOwner: SetOwner): ResultSetFuture = {
    val boundUpdate = bndUpdateOwner(setOwnerStmt, setOwner)
    session.executeAsync(boundUpdate)
  }

  /** Query ShoppingCart by id, then update its owner with version returned by query and owner in setOwner
    *
    * A Cassandra lightweight transaction with optimistic locking
    *
    * @param session: Session
    * @param queryStmt: PreparedStatement
    * @param setStmt: PreparedStatement
    * @param setOwner: SetOwner in 2nd arg list, allows currying
    * @return ResultSet
    */
  def checkAndSetOwner(session: Session, queryStmt: PreparedStatement, setStmt: PreparedStatement)(setOwner: SetOwner):
    ResultSetFuture = {
    val sc = getShoppingCart(session, queryStmt, setOwner.cartId)
    sc match {
      case Some(x) => updateOwner(session, setStmt, SetOwner(setOwner.cartId, setOwner.owner, x.version))
      case None => updateOwner(session, setStmt, SetOwner(setOwner.cartId, setOwner.owner, 1))
    }
  }

  /** Update ShoppingCart items, asynchronous
    *
    * @param session
    * @param setItemsStmt: PreparedStatement
    * @param setItems: SetItems
    * @return ResultSet
    */
  def updateItems(session: Session, setItemsStmt: PreparedStatement, setItems: SetItems): ResultSetFuture = {
    val boundUpdate = bndUpdateItems(setItemsStmt, setItems)
    session.executeAsync(boundUpdate)
  }

  /** Query ShoppingCart by id, then update its items with version returned by query and items in setItems
    *
    * A Cassandra lightweight transaction with optimistic locking
    *
    * @param session: Session
    * @param queryStmt: PreparedStatement
    * @param setStmt: PreparedStatement
    * @param setItems: SetItems in 2nd arg list, allows currying
    * @return ResultSet
    */
  def checkAndSetItems(session: Session, queryStmt: PreparedStatement, setStmt: PreparedStatement)(setItems: SetItems):
    ResultSetFuture = {
    val sc = getShoppingCart(session, queryStmt, setItems.cartId)
    sc match {
      case Some(x) => updateItems(session, setStmt, SetItems(setItems.cartId, setItems.items, x.version))
      case None => updateItems(session, setStmt, SetItems(setItems.cartId, setItems.items, 1))
    }
  }

  /** Map Row to case class. Uses ScalaCass object mapping
    *
    * @note Eclipse reports an error because it doesn't find a ScalaCass implicit but it's not an error.
    *
    * @param row
    * @return case class
    */
  def mapRow(row: Row): ShoppingCart = row.as[ShoppingCart]

  /** Map Seq of Rows to case class.

    *
    * @param rows: Seq[Row]
    * @return Seq[ShoppingCart]
    */
  def mapRows(rows: Seq[Row]): Seq[ShoppingCart] = rows.map { x => mapRow(x) }

}
