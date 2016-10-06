Non-blocking calls to parallel services and combine responses.
![image](https://github.com/garyaiki/dendrites/blob/master/docs/png/ParallelHttpFlow.png?raw=true)

[ZipWith](http://doc.akka.io/docs/akka/2.4/scala/stream/stages-overview.html#zipWithN) takes outputs of all services and pushes them in a tuple when they are all ready.

```scala
def zipper = ZipWith((in0: Either[String, AnyRef],
                in1: Either[String, AnyRef],
                in2: Either[String, AnyRef]) => (in0, in1, in2))

val ccf = new CheckingCallFlow
val mmcf = new MoneyMarketCallFlow
val scf = new SavingsCallFlow

import GraphDSL.Implicits._
// Create Graph in Shape of a Flow
val flowGraph = GraphDSL.create() { implicit builder =>
val bcast: UniformFanOutShape[Product, Product] =     builder.add(Broadcast[Product](3))
val check: FlowShape[Product,Either[String, AnyRef]] = builder.add(ccf.flow)
val mm: FlowShape[Product,Either[String, AnyRef]] = builder.add(mmcf.flow)
val savings: FlowShape[Product,Either[String, AnyRef]] = builder.add(scf.flow)
val zip = builder.add(zipper)

bcast ~> check ~> zip.in0
bcast ~> mm ~> zip.in1
bcast ~> savings ~> zip.in2
FlowShape(bcast.in, zip.out)
}.named("calls")
// Cast Graph to Flow
val asFlow = Flow.fromGraph(flowGraph)
// Map tuple3 from flowGraph
val fgLR = GraphDSL.create() { implicit builder =>
val fgCalls = builder.add(asFlow)
val fgLR = builder.add(leftRightFlow) // results combiner

fgCalls ~> fgLR
FlowShape(fgCalls.in, fgLR.outlet)
}.named("callsLeftRight")
val wrappedCallsLRFlow = Flow.fromGraph(fgLR)
```