package org.gs.algebird.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.{CMS, CMSHasher, CMSMonoid}
import org.gs.algebird.{createCMSMonoid, createCountMinSketch}

/** Flow to create CMS
  *
  * @author Gary Struthers
  * @tparam K with Ordering and CMSHasher
  */
class CreateCMSFlow[K: Ordering: CMSHasher] extends GraphStage[FlowShape[Seq[K], CMS[K]]] {

  val in = Inlet[Seq[K]]("K => CMS in")
  val out = Outlet[CMS[K]]("CMS out")
  override val shape = FlowShape.of(in, out)

  implicit val monoid: CMSMonoid[K] = createCMSMonoid[K]()

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          push(out, createCountMinSketch(elem))
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
