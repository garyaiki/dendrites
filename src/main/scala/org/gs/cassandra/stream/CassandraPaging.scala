package org.gs.cassandra.stream

import akka.event.LoggingAdapter
import akka.stream.{Attributes, FlowShape, Inlet, Outlet }
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.datastax.driver.core.{ResultSet, Row}
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.mutable.ArrayBuffer

/** Page specified number of Rows from a ResultSet 
  *
  * @author Gary Struthers
  * @param pageSize number of Rows to send downstream
  * @param implicit logger
  */
class CassandraPaging(pageSize: Int)(implicit logger: LoggingAdapter)
    extends GraphStage[FlowShape[ResultSet, Seq[Row]]]{

  val in = Inlet[ResultSet]("CassandraPaging.in")
  val out = Outlet[Seq[Row]]("CassandraPaging.out")
  override val shape = FlowShape.of(in, out)

  /** When a ResultSet is pushed from upstream get its iterator and push
    * pageSize number of Rows. When downstream pulls, forward pull upstream if iterator is empty. If
    * iterator has elements push pageSize number of Rows. When iterator isEmpty set Iterator.empty
    * 
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      var it: Iterator[Row] = Iterator.empty
      
      def pageRows(): Seq[Row] = {
        val pageIt = it.take(pageSize)
        if(it.isEmpty) it = Iterator.empty
        pageIt.toSeq
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          it = grab(in).iterator()
          push(out, pageRows())
        }
      })
      
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if(it.isEmpty) {
            pull(in)
          } else {
            push(out, pageRows())
          }
        }
      })
    }
  }
}
