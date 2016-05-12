package org.gs.cassandra.stream

import akka.event.LoggingAdapter
import akka.stream.{Attributes, FlowShape, Inlet, Outlet }
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.datastax.driver.core.{ResultSet, Row}
import java.util.{Iterator => JIterator}
import scala.collection.mutable.ArrayBuffer

/** Page specified number of Rows from a ResultSet 
  *
  * @author Gary Struthers
  * @pageSize number of Rows to send downstream
  * @implicit logger
  */
class CassandraPaging(pageSize: Int)(implicit logger: LoggingAdapter)
    extends GraphStage[FlowShape[ResultSet, Seq[Row]]]{

  val in = Inlet[ResultSet]("CassandraPaging.in")
  val out = Outlet[Seq[Row]]("CassandraPaging.out")
  override val shape = FlowShape.of(in, out)

  /** When a ResultSet is pushed from upstream get its Java iterator and push
    * pageSize number of Rows. When downstream pulls, forward pull upstream if iterator is null. If
    * iterator exists push pageSize number of Rows. When iterator has no more elements set it null
    * 
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      var it: JIterator[Row] = null
      
      def pageRows(): Seq[Row] = {
        var countDown = pageSize
        val rowBuf = new ArrayBuffer[Row](countDown)
        while(countDown > 0 && it.hasNext()) {
          rowBuf.append(it.next())
          countDown = countDown -1
        }
        if(!it.hasNext()) it = null
        rowBuf.toSeq
      }
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          it = grab(in).iterator()
          push(out, pageRows())
        }
      })
      
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if(it == null) {
            pull(in)
          } else {
            push(out, pageRows())
          }
        }
      })
    }
  }
}
