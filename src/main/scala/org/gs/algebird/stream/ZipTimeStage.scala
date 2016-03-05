package org.gs.algebird.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.github.nscala_time.time.StaticDateTime

class ZipTimeStage[A: Numeric] extends GraphStage[FlowShape[Seq[A], Seq[(Double, Double)]]] {

  val in = Inlet[Seq[A]]("ZipTimeStage in")
  val out = Outlet[Seq[(Double, Double)]]("ZipTimeStage out")
  override val shape = FlowShape.of(in, out)
  val doubleTime = StaticDateTime.now.getMillis.toDouble

  def toZipTime(xs: Seq[A]): Seq[(Double, Double)] =
    xs.map(x => (x.asInstanceOf[Number].doubleValue(), doubleTime))

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          push(out, toZipTime(elem))
        }
      })
  
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
  }
 // override def onPush(elem: Seq[A], ctx: Context[Seq[(Double, Double)]]): SyncDirective = ctx.push(toZipTime(elem))
}