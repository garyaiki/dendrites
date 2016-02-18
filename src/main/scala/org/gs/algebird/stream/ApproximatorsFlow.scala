package org.gs.algebird.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.{Materializer, FlowShape, UniformFanOutShape}
import akka.stream.scaladsl.{ Broadcast, Flow, GraphDSL, ZipWith}
import akka.stream.scaladsl.GraphDSL.Implicits._
import com.twitter.algebird.{AveragedValue, CMS, CMSHasher, DecayedValue, HLL, QTree}
import scala.reflect.runtime.universe.TypeTag
import org.gs.algebird.agent.{AveragedAgent, CountMinSketchAgent,DecayedValueAgent,HyperLogLogAgent,
  QTreeAgent}
import org.gs.algebird.typeclasses.HyperLogLogLike

class ApproximatorsFlow[A: HyperLogLogLike: Numeric: CMSHasher:  TypeTag](
    avgAgent: AveragedAgent,
    cmsAgent: CountMinSketchAgent[A],
    dcaAgent: DecayedValueAgent,
    hllAgent: HyperLogLogAgent,
    qtrAgent: QTreeAgent[A])
  (implicit val system: ActorSystem, logger: LoggingAdapter, val materializer: Materializer) {

  def zipper = ZipWith((in0: AveragedValue,
                        in1: CMS[A],
                        in2: Seq[DecayedValue],
                        in3: HLL,
                        in4: QTree[A]) => (in0, in1, in2, in3, in4))

  val avgAgentAlter = avgAgent.alter _
  val cmsAgentAlter = cmsAgent.alter _
  val dcaAgentAlter = dcaAgent.alter _
  val hllAgentAlter = hllAgent.alter _
  val qtrAgentAlter = qtrAgent.alter _
  
  def avgAgflow: Flow[AveragedValue, AveragedValue, NotUsed] =
        Flow[AveragedValue].mapAsync(1)(avgAgentAlter)

  def cmsAgflow: Flow[CMS[A], CMS[A], NotUsed] = Flow[CMS[A]].mapAsync(1)(cmsAgentAlter)

  def dcaAgFlow: Flow[Seq[(Double, Double)], Seq[DecayedValue], NotUsed] =
        Flow[Seq[(Double, Double)]].mapAsync(1)(dcaAgentAlter)

  def hllAgflow: Flow[HLL, HLL, NotUsed] = Flow[HLL].mapAsync(1)(hllAgentAlter)

  def qtrAgFlow: Flow[Seq[A], QTree[A], NotUsed] = Flow[Seq[A]].mapAsync(1)(qtrAgentAlter)

  val approximators = GraphDSL.create() { implicit builder =>
    val bcast: UniformFanOutShape[Seq[A], Seq[A]] = builder.add(Broadcast[Seq[A]](5))
    val avg: FlowShape[Seq[A], AveragedValue] = builder.add(avgFlow)
    val avgAg: FlowShape[AveragedValue, AveragedValue] = builder.add(avgAgflow)
    val cms: FlowShape[Seq[A], CMS[A]] =
            builder.add(Flow[Seq[A]].transform(() => new CreateCMSStage))
    val cmsAg: FlowShape[CMS[A], CMS[A]] = builder.add(cmsAgflow)
    val dvt: FlowShape[Seq[A], Seq[(Double, Double)]] =
            builder.add(Flow[Seq[A]].transform(() => new ZipTimeStage))
    val dcaAg: FlowShape[Seq[(Double, Double)], Seq[DecayedValue]] = builder.add(dcaAgFlow)
    val hll: FlowShape[Seq[A], HLL] =
            builder.add(Flow[Seq[A]].transform(() => new CreateHLLStage[A]))
    val hllAg: FlowShape[HLL, HLL] = builder.add(hllAgflow)
    val qtrAg: FlowShape[Seq[A], QTree[A]] = builder.add(qtrAgFlow)
    val zip = builder.add(zipper)

    bcast ~> avg ~> avgAg ~> zip.in0
    bcast ~> cms ~> cmsAg ~> zip.in1
    bcast ~> dvt ~> dcaAg ~> zip.in2
    bcast ~> hll ~> hllAg ~> zip.in3
    bcast ~> qtrAg        ~> zip.in4
    FlowShape(bcast.in, zip.out)
  }.named("approximators")
}
