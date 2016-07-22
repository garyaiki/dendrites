/** Copyright 2016 Gary Struthers

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
package org.gs.algebird.agent.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.{FlowShape, Graph, Materializer, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, ZipWith}
import akka.stream.scaladsl.GraphDSL.Implicits._
import com.twitter.algebird.{AveragedValue, CMS, CMSHasher, DecayedValue, HLL, QTree}
import scala.concurrent.Future
import scala.reflect.runtime.universe.TypeTag
import org.gs.algebird.agent.{AveragedAgent,
  CountMinSketchAgent,
  DecayedValueAgent,
  HyperLogLogAgent,
  QTreeAgent}
import org.gs.algebird.stream.{CreateCMSFlow, CreateHLLFlow}
import org.gs.algebird.typeclasses.{HyperLogLogLike, QTreeLike}

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
  * @param time function generates time as doubles for DecayedValue
  */
class ParallelApproximators[A: HyperLogLogLike: Numeric: CMSHasher: QTreeLike: TypeTag](
    avgAgent: AveragedAgent,
    cmsAgent: CountMinSketchAgent[A],
    dcaAgent: DecayedValueAgent,
    hllAgent: HyperLogLogAgent,
    qtAgent: QTreeAgent[A],
    time:A => Double)
  (implicit val system: ActorSystem, logger: LoggingAdapter, val materializer: Materializer) {

  // Zip input agent update Futures, waits for all to complete
  def zipper = ZipWith((in0: Future[AveragedValue],
                        in1: Future[CMS[A]],
                        in2: Future[Seq[DecayedValue]],
                        in3: Future[HLL],
                        in4: Future[QTree[A]]) => (in0, in1, in2, in3, in4))

  // Graph to broadcast to update agent composite sinks
  val approximators = GraphDSL.create() { implicit builder =>
    val bcast: UniformFanOutShape[Seq[A], Seq[A]] = builder.add(Broadcast[Seq[A]](5))
    val avg = builder.add(AveragedAgentFlow.compositeFlow(avgAgent))
    val cms = builder.add(CountMinSketchAgentFlow.compositeFlow(cmsAgent))
    val dvt = builder.add(DecayedValueAgentFlow.compositeFlow(dcaAgent, time))
    val hll = builder.add(HyperLogLogAgentFlow.compositeFlow(hllAgent))
    val qtaf = new QTreeAgentFlow(qtAgent)
    val qtrAg = builder.add(qtaf)
    val zip = builder.add(zipper)

    bcast ~> avg ~> zip.in0
    bcast ~> cms ~> zip.in1
    bcast ~> dvt ~> zip.in2
    bcast ~> hll ~> zip.in3
    bcast ~> qtrAg ~> zip.in4
    FlowShape(bcast.in, zip.out)
  }.named("parallelApproximators")
}

object ParallelApproximators {

  def compositeFlow[A: HyperLogLogLike: Numeric: CMSHasher: QTreeLike: TypeTag](
    avgAgent: AveragedAgent,
    cmsAgent: CountMinSketchAgent[A],
    dvAgent: DecayedValueAgent,
    hllAgent: HyperLogLogAgent,
    qtAgent: QTreeAgent[A],
    time:A => Double)
    (implicit system: ActorSystem, logger: LoggingAdapter, materializer: Materializer):
            Graph[FlowShape[Seq[A], (Future[AveragedValue],
                                              Future[CMS[A]],
                                              Future[Seq[DecayedValue]],
                                              Future[HLL],
                                              Future[QTree[A]])], NotUsed] = {
    val pa = new ParallelApproximators[A](avgAgent,
        cmsAgent,
        dvAgent,
        hllAgent,
        qtAgent,
        DecayedValueAgentFlow.nowMillis)
    pa.approximators
  }

  def compositeSink[A: HyperLogLogLike: Numeric: CMSHasher: QTreeLike: TypeTag](
    avgAgent: AveragedAgent,
    cmsAgent: CountMinSketchAgent[A],
    dvAgent: DecayedValueAgent,
    hllAgent: HyperLogLogAgent,
    qtAgent: QTreeAgent[A],
    time:A => Double)
    (implicit system: ActorSystem, logger: LoggingAdapter, materializer: Materializer):
        Sink[Seq[A], NotUsed] = {
    
      val composite = compositeFlow[A](avgAgent, cmsAgent, dvAgent, hllAgent, qtAgent, time)
      val ffg = Flow.fromGraph(composite)
      ffg.to(Sink.ignore).named("parallelApproximatorsSink")
  }
}
