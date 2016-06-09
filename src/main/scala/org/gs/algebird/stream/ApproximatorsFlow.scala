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

/** Update Algebird approximators concurrently
  * Input a Seq[A] broadcast it to Agents for Algebird approximaters then zip the latest values of
  * the agents.
  *
  * @author Gary Struthers
  *
  * @tparam <A> with implicitl HyperLogLogLike[A], Numeric[A] and CMSHasher[A]
  * @param avgAgent AveragedAgent
  * @param cmsAgent: CountMinSketchAgent
  * @param dcaAgent: DecayedValueAgent
  * @param hllAgent: HyperLogLogAgent
  * @param qtrAgent: QTreeAgent
  */
class ApproximatorsFlow[A: HyperLogLogLike: Numeric: CMSHasher: TypeTag](
    avgAgent: AveragedAgent,
    cmsAgent: CountMinSketchAgent[A],
    dcaAgent: DecayedValueAgent,
    hllAgent: HyperLogLogAgent,
    qtrAgent: QTreeAgent[A])
  (implicit val system: ActorSystem, logger: LoggingAdapter, val materializer: Materializer) {

  // Zip input agent update Futures, waits for all to complete
  def zipper = ZipWith((in0: AveragedValue,
                        in1: CMS[A],
                        in2: Seq[DecayedValue],
                        in3: HLL,
                        in4: QTree[A]) => (in0, in1, in2, in3, in4))

  // Agent update functions, partially applied so they can be passed to mapAsync
  val avgAgentAlter = avgAgent.alter _
  val cmsAgentAlter = cmsAgent.alter _
  val dcaAgentAlter = dcaAgent.alter _
  val hllAgentAlter = hllAgent.alter _
  val qtrAgentAlter = qtrAgent.alter _

  //Asychronous flow stages to update agents 
  def avgAgflow: Flow[AveragedValue, AveragedValue, NotUsed] =
        Flow[AveragedValue].mapAsync(1)(avgAgentAlter)

  def cmsAgflow: Flow[CMS[A], CMS[A], NotUsed] = Flow[CMS[A]].mapAsync(1)(cmsAgentAlter)

  def dcaAgFlow: Flow[Seq[(Double, Double)], Seq[DecayedValue], NotUsed] =
        Flow[Seq[(Double, Double)]].mapAsync(1)(dcaAgentAlter)

  def hllAgflow: Flow[HLL, HLL, NotUsed] = Flow[HLL].mapAsync(1)(hllAgentAlter)

  def qtrAgFlow: Flow[Seq[A], QTree[A], NotUsed] = Flow[Seq[A]].mapAsync(1)(qtrAgentAlter)

  // Graph to broadcast to update agent flow stages then zip results then cast to FlowShape
  val approximators = GraphDSL.create() { implicit builder =>
    val bcast: UniformFanOutShape[Seq[A], Seq[A]] = builder.add(Broadcast[Seq[A]](5))
    val avg: FlowShape[Seq[A], AveragedValue] = builder.add(avgFlow)
    val avgAg: FlowShape[AveragedValue, AveragedValue] = builder.add(avgAgflow)
    val cms: FlowShape[Seq[A], CMS[A]] = builder.add(new CreateCMSFlow)
    val cmsAg: FlowShape[CMS[A], CMS[A]] = builder.add(cmsAgflow)
    val dvt: FlowShape[Seq[A], Seq[(Double, Double)]] = builder.add(new ZipTimeFlow)
    val dcaAg: FlowShape[Seq[(Double, Double)], Seq[DecayedValue]] = builder.add(dcaAgFlow)
    val hll: FlowShape[Seq[A], HLL] = builder.add(new CreateHLLFlow[A])
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
