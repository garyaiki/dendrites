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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart

import akka.event.LoggingAdapter
import com.datastax.driver.core.{PreparedStatement, ResultSetFuture, Session}
import com.datastax.driver.core.utils.UUIDs.timeBased

import java.util.UUID
import com.github.garyaiki.dendrites.cqrs.Event
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.bndInsert
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd

package object event {
// Events
case class OwnerChanged(cartId: UUID, time: UUID, eventID: UUID, owner: UUID) extends Event with Cart
case class ItemAdded(cartId: UUID, time: UUID, eventID: UUID, item: UUID, count: Int) extends Event with Cart
case class ItemRemoved(cartId: UUID, time: UUID, eventID: UUID, item: UUID, count: Int) extends Event with Cart
// Event log
case class ShoppingCartEvt(cartId: UUID, time: UUID, eventID: UUID, owner: Option[UUID], item: Option[UUID],
  count: Option[Int]) extends Event with Cart

  /** Convert Kafka key and ShoppingCartCmd to a ShoppingCartEvt
    *
    * @param kafkaKey must be convertible to a UUID
    * @param cmd ShoppingCartCmd
    * @param logger
    * @return ShoppingCartEvt
    * @throws IllegalArgumentException if kafkaKey can't be converted to a UUID
    */
  def cmdToEvt(kafkaKey: String, cmd: ShoppingCartCmd)(implicit logger: LoggingAdapter): ShoppingCartEvt = {
    try {
      val evtId = UUID.fromString(kafkaKey)
      val now = timeBased
      cmd.count match {
        case None => ShoppingCartEvt(cmd.cartId, now, evtId, Some(cmd.ownerOrItem), None, None)
        case Some(count) => ShoppingCartEvt(cmd.cartId, now, evtId, None, Some(cmd.ownerOrItem), Some(count))
      }
    } catch {
      case e: IllegalArgumentException => {
        logger.error(e, e.getMessage)
        throw e
      }
      case e: Throwable => {
        logger.error(e, e.getMessage)
        throw e
      }
    }
  }

  /** Insert ShoppingCart Events into Cassandra
    *
    * @tparam A case class implements Event with Cart
    * @param session Cassandra Session
    * @param stmt Prepared insert statement
    * @param evt ShoppingCart event case class
    * @return ResultSetFuture
    */
  def doShoppingCartEvt[A <: Event with Cart](session: Session, stmt: PreparedStatement)(evt: A): ResultSetFuture
    = {
    evt match {
      case oc: OwnerChanged => {
        val cc = ShoppingCartEvt(oc.cartId, oc.time, oc.eventID, Some(oc.owner), None, None)
        val bs = bndInsert(stmt, cc)
        session.executeAsync(bs)
      }
      case ia: ItemAdded => {
        val cc = ShoppingCartEvt(ia.cartId, ia.time, ia.eventID, None, Some(ia.item), Some(ia.count))
        val bs = bndInsert(stmt, cc)
        session.executeAsync(bs)
      }
      case ir: ItemRemoved => {
        val cc = ShoppingCartEvt(ir.cartId, ir.time, ir.eventID, None, Some(ir.item), Some(ir.count))
        val bs = bndInsert(stmt, cc)
        session.executeAsync(bs)
      }
      case sc: ShoppingCartEvt => {
        val bs = bndInsert(stmt, sc)
        session.executeAsync(bs)
      }
    }
  }

  /** Group duplicate events as Seq values with eventId as keys
    *
    * @param xs Sequence of ShoppingCartEvt
    * @return map key is eventId with Sequence of events for that id
    */
  def groupByEvtId(xs: Seq[ShoppingCartEvt]): Map[UUID, Seq[ShoppingCartEvt]] = {
    xs.groupBy[UUID](_.eventID)
  }

  /** Remove events without duplicates and removed owner changed duplicates (owner changed is idempotent)
    *
    * @param evtMap
    * @return filtered map
    */
  def filterDups(evtMap : Map[UUID, Seq[ShoppingCartEvt]]): Map[UUID, Seq[ShoppingCartEvt]] = {
    for {
      evt <- evtMap
      if(evt._2.length > 1)
      if(evt._2.forall(_.owner == None))
    } yield evt
  }

  /** Compensate for duplicate add or remove item events
    *
    * @param xs ShoppingCart events, may have duplicates
    * @return compensating ShoppingCart events
    */
  def compensateDupEvt(xs: Seq[ShoppingCartEvt]): Option[Seq[ShoppingCartEvt]] = {
    val grouped = groupByEvtId(xs)
    System.out.println(s"grouped size:${grouped.size}")
    val itemDups = filterDups(grouped)
    System.out.println(s"itemDups size:${itemDups.size}")
    val ys = for {
      evt <- itemDups
      values = evt._2
      sc = values(0)
      count = sc.count.getOrElse(0) * values.length
      if(count != 0)
      compensatedCount = if(count > 0) (count -1) * -1 else (count + 1) * -1
    } yield ShoppingCartEvt(sc.cartId, sc.time, sc.eventID, None, sc.item, Some(compensatedCount))

    val zs = ys.toSeq
    if(zs.isEmpty) None else Some(zs)
  }
}
