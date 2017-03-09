/** Copyright 2016 - 2017 Gary Struthers
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.github.garyaiki.dendrites.cassandra.stream

import akka.event.LoggingAdapter
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.datastax.driver.core.{ResultSet, Row}
import scala.collection.JavaConversions.asScalaIterator

/** Map a Page of specified number of Rows from a ResultSet to case classes. Same as CassandraPaging & Map in 1 stage
  *
  * @tparam A case class to map to
  * @param size number to send downstream
  * @param f map function
  * @param logger implicit LoggingAdapter
  * @author Gary Struthers
  */
class CassandraMappedPaging[A <: Product](size: Int, f: Seq[Row] => Seq[A])(implicit logger: LoggingAdapter) extends
  GraphStage[FlowShape[ResultSet, Seq[A]]] {

  val in = Inlet[ResultSet]("CassandraMappedPaging.in")
  val out = Outlet[Seq[A]]("CassandraMappedPaging.out")
  override val shape = FlowShape.of(in, out)

  /** When a ResultSet is pushed from upstream get its iterator and map pageSize Seq of Rows to Seq of case classes.
    * When downstream pulls, forward pull upstream if iterator is empty. If iterator has elements push pageSize Seq
    * of case classes. When iterator isEmpty set Iterator.empty
    *
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      var it: Iterator[Row] = Iterator.empty

      def pageRows(): Seq[Row] = {
        val pageIt = it.take(size)
        if (it.isEmpty) it = Iterator.empty
        pageIt.toSeq
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          it = grab(in).iterator
          val rows = pageRows
          push(out, f(rows))
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if (it.isEmpty) {
            pull(in)
          } else {
            val rows = pageRows
            push(out, f(rows))
          }
        }
      })
    }
  }
}
