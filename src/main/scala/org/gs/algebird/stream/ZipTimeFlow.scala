package org.gs.algebird.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import java.time.Instant

class ZipTimeFlow[A: Numeric] extends GraphStage[FlowShape[Seq[A], Seq[(Double, Double)]]] {

  val in = Inlet[Seq[A]]("ZipTimeFlow in")
  val out = Outlet[Seq[(Double, Double)]]("ZipTimeFlow out")
  override val shape = FlowShape.of(in, out)

  def toZipTime(xs: Seq[A]): Seq[(Double, Double)] =
    xs.map(x => (x.asInstanceOf[Number].doubleValue(), Instant.now.toEpochMilli.toDouble))

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
}