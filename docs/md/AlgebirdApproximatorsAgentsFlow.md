Combine Twitter [Algebird](https://github.com/twitter/algebird) approximators with Akka [Agents](http://doc.akka.io/docs/akka/2.4/scala/agents.html)
![image](https://github.com/garyaiki/dendrites/blob/master/docs/png/AlgebirdApproximatorsAgentsFlow.png?raw=true)
[AveragedValue](https://github.com/twitter/algebird/blob/develop/algebird-core/src/main/scala/com/twitter/algebird/AveragedValue.scala), [CountMinSketch](https://github.com/twitter/algebird/blob/develop/algebird-core/src/main/scala/com/twitter/algebird/CountMinSketch.scala), [DecayedValue](https://github.com/twitter/algebird/blob/develop/algebird-core/src/main/scala/com/twitter/algebird/DecayedValue.scala), [HyperLogLog](https://github.com/twitter/algebird/blob/develop/algebird-core/src/main/scala/com/twitter/algebird/HyperLogLog.scala), and [QTree](https://github.com/twitter/algebird/blob/develop/algebird-core/src/main/scala/com/twitter/algebird/QTree.scala) are fast hashing approximators. Agents make these running totals globally readable and updatable.

```scala
// Zip input agent update Futures, waits for all to complete
def zipper: ZipWith5[Future[AveragedValue], Future[CMS[A]], Future[Seq[DecayedValue]], Future[HLL], Future[QTree[A]], (Future[AveragedValue], Future[CMS[A]], Future[Seq[DecayedValue]], Future[HLL], Future[QTree[A]])] = ZipWith((in0: Future[AveragedValue],
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
```