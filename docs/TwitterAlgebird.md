### Algebird streaming approximations

{% include nav.html %}
Aggregate streaming data with pre-built Algebird stages and functions.

[<img src="png/AvgFlow.png?raw=true" alt="AvgFlow" width="20%" height="20%" title="input a sequence of Numeric types, output their AveragedValue">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/sumAvgFlow.png?raw=true" alt="sumAvgFlow" width="20%" height="20%" title="input sequence of AveragedValue, output single AveragedValue">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/CMSFlow.png?raw=true" alt="CMSFlow" width="20%" height="20%" title="input a sequence of values that can be Ordered and CMSHashed, output their CountMinSketch">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/CreateCMSFlow.scala){:target="_blank"}
[<img src="png/ZipTimeFlow.png?raw=true" alt="ZipTimeFlow" width="20%" height="20%" title="input sequence of Numeric values, output sequence of tuple of values and timestamp, for DecayedValue">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/ZipTimeFlow.scala){:target="_blank"}
[<img src="png/HLLFlow.png?raw=true" alt="HLLFlow" width="20%" height="20%" title="input case class that is HyperLogLogLike, output an HLL">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/CreateHLLFlow.scala){:target="_blank"}
[<img src="png/estSizeFlow.png?raw=true" alt="estSizeFlow" width="20%" height="20%" title="input HLL, output estimated size of HLL">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/sumHLLs.png?raw=true" alt="sumHLLs" width="20%" height="20%" title="input sequence of HLL, output Approximate value">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/toApproximate.png?raw=true" alt="toApproximate" width="20%" height="20%" title="input HLL, output Approximate">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/toApproximates.png?raw=true" alt="toApproximates" width="20%" height="20%" title="input sequence of HLL, output sequence of Approximate">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/maxFlow.png?raw=true" alt="maxFlow" width="20%" height="20%" title="input sequence of values that are Ordered, output sequences max value">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/minFlow.png?raw=true" alt="minFlow" width="20%" height="20%" title="input sequence of values that are Ordered, output sequences min value">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/firstQuartileFlow.png?raw=true" alt="firstQuartileFlow" width="20%" height="20%" title="input value that's QTree like, output tuple of 1st quartile upper, lower bounds">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/secondQuartileFlow.png?raw=true" alt="secondQuartileFlow" width="20%" height="20%" title="input value that's QTree like, output tuple of 2nd quartile upper, lower bounds">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/thirdQuartileFlow.png?raw=true" alt="thirdQuartileFlow" width="20%" height="20%" title="input value that's QTree like, output tuple of 3rd quartile upper, lower bounds">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/interQuartileMean.png?raw=true" alt="interQuartileMean" width="20%" height="20%" title="input value that's QTree like, output tuple of inter quartile mean's upper, lower bounds">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/qTreeMaxFlow.png?raw=true" alt="qTreeMaxFlow" width="20%" height="20%" title="input sequence of values that are QTree like,  output QTree's max value">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
[<img src="png/qTreeMinFlow.png?raw=true" alt="qTreeMinFlow" width="20%" height="20%" title="input sequence of values that are QTree like,  output QTree's min value">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/stream/package.scala){:target="_blank"}
###### Click image to open source code in a new tab. Hover over image for stage inputs and outputs

Hash streaming values for near realtime approximate statistics.

[AveragedValue](https://twitter.github.io/algebird/datatypes/averaged_value.html){:target="_blank"} estimates a variable's mean in the stream.

[CountMinSketch](https://twitter.github.io/algebird/datatypes/approx/countminsketch.html){:target="_blank"} estimates a variable's frequency in the stream.

[DacayedValue](https://twitter.github.io/algebird/datatypes/decayed_value.html){:target="_blank"} estimates a variable's moving average in the stream. The value is tupled with a timestamp in ZipTimeFlow. Halflife de-weights values by age.

[HyperLogLog](https://twitter.github.io/algebird/datatypes/approx/hyperloglog.html){:target="_blank"} estimates a variable's distinct values in the stream.

[Min and Max](https://twitter.github.io/algebird/datatypes/min_and_max.html){:target="_blank"} estimates a variable's minimum or maximum values in the stream.

[QTree](https://twitter.github.io/algebird/datatypes/approx/q_tree.html){:target="_blank"} estimates quartiles for a variable in the stream.

[BloomFilter](https://twitter.github.io/algebird/datatypes/approx/bloom_filter.html){:target="_blank"} quickly ensures a word is *not* in a dictionary or a set of words and quickly predicts a word is *probably* in a dictionary or a set of words

[Algebird](https://github.com/twitter/algebird){:target="_blank"} approximators can stream in parallel. [Agents](http://doc.akka.io/docs/akka/current/scala/agents.html){:target="_blank"} can be used to make these approximations globally readable and updatable but they are deprecated in Akka.

![image](png/AlgebirdApproximatorsAgentsFlow.png?raw=true)

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


