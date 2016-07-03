package org.gs.algebird.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

/** Flow that generates elem/time tuples from elem. Meant for DecayedValues
  *
  * @tparam A Numeric
  * @param f produces time, arg:A is ignoreable
  * @author Gary Struthers
  */
class ZipTimeFlow[A: Numeric](f: A => Double)
        extends GraphStage[FlowShape[Seq[A], Seq[(Double, Double)]]] {

  val in = Inlet[Seq[A]]("ZipTimeFlow in")
  val out = Outlet[Seq[(Double, Double)]]("ZipTimeFlow out")
  override val shape = FlowShape.of(in, out)

  def toZipTime(xs: Seq[A]): Seq[(Double, Double)] =
    xs.map(x => (x.asInstanceOf[Number].doubleValue(), f(x)))// Instant.now.toEpochMilli.toDouble))

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