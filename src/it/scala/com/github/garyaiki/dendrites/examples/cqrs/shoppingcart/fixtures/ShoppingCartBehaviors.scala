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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.fixtures

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{PreparedStatement, Session}
import java.util.UUID
import org.scalatest.{Matchers, WordSpecLike}
import scala.collection.immutable.Iterable
import scala.concurrent.ExecutionContext
import com.github.garyaiki.dendrites.cassandra.stream.{CassandraBind, CassandraBoundQuery, CassandraMappedPaging,
  CassandraSink}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.ShoppingCart
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCart.{bndDelete, bndQuery,
  mapRows}

trait ShoppingCartBehaviors extends Matchers with ShoppingCartCmdBuilder { this: WordSpecLike =>

  def queryShoppingCart(session: Session, prepStmts: Map[String, PreparedStatement])
    (implicit sys: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer, logger: LoggingAdapter): Seq[ShoppingCart]
      = {

    val source = TestSource.probe[UUID]
    val prepStmt = prepStmts.get("Query") match {
      case Some(stmt) => stmt
      case None       => fail("CassandraShoppingCart Query PreparedStatement not found")
    }
    val query = new CassandraBoundQuery[UUID](session, prepStmt, bndQuery, 1)
    val paging = new CassandraMappedPaging[ShoppingCart](10, mapRows)
    def sink = TestSink.probe[Seq[ShoppingCart]]
    val (pub, sub) = source.via(query).via(paging).toMat(sink)(Keep.both).run()
    sub.request(1)
    pub.sendNext(cartId)
    val response = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()

    response
  }

  def deleteShoppingCart(session: Session, prepStmts: Map[String, PreparedStatement])(implicit ec: ExecutionContext,
    mat: ActorMaterializer, logger: LoggingAdapter) {

    val cartIds: Seq[UUID] = Seq(cartId)
    val iter = Iterable(cartIds.toSeq: _*)
    val source = Source[UUID](iter)
    val prepStmt = prepStmts.get("Delete") match {
      case Some(stmt) => stmt
      case None       => fail("CassandraShoppingCart Delete PreparedStatement not found")
    }
    val bndStmt = new CassandraBind(prepStmt, bndDelete)
    val sink = new CassandraSink(session)
    source.via(bndStmt).runWith(sink)
  }
}
