### Algebird streaming approximations

{% include nav.html %}

Near realtime approximations of stream data with pre-built [Algebird](https://twitter.github.io/algebird/){:target="_blank"} stages and stand-alone functions.

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

#### Stream Stages

[AveragedValue](https://twitter.github.io/algebird/datatypes/averaged_value.html){:target="_blank"} estimates a variable's mean in the stream.

[CountMinSketch](https://twitter.github.io/algebird/datatypes/approx/countminsketch.html){:target="_blank"} estimates a variable's frequency.

[DecayedValue](https://twitter.github.io/algebird/datatypes/decayed_value.html){:target="_blank"} estimates a variable's moving average and de-weights values by age. The value is tupled with a timestamp in ZipTimeFlow.

[HyperLogLog](https://twitter.github.io/algebird/datatypes/approx/hyperloglog.html){:target="_blank"} estimates a variable's number of distinct values.

[Min and Max](https://twitter.github.io/algebird/datatypes/min_and_max.html){:target="_blank"} estimates a variable's minimum or maximum values.

[QTree](https://twitter.github.io/algebird/datatypes/approx/q_tree.html){:target="_blank"} estimates quartiles for a variable.

[BloomFilter](https://twitter.github.io/algebird/datatypes/approx/bloom_filter.html){:target="_blank"} quickly ensures a word is *not* in a dictionary or a set of words and quickly predicts a word is *probably* in a dictionary or a set of words

[Algebird](https://github.com/twitter/algebird){:target="_blank"} approximators can stream in parallel. This example uses [Agents](http://doc.akka.io/docs/akka/current/scala/agents.html){:target="_blank"} which are deprecated in Akka 2.5.

<img src="png/AlgebirdApproximatorsAgentsFlow.png?raw=true" width="80%" />

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

#### Stand-alone Functions

[Approximating Functions](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/algebird/package.scala){:target="_blank"}

```scala
val bigDecimals: Seq[BigDecimal]
val avg0 = avg(bigDecimals)
```
###### AveragedValue of a Sequence of values
```scala
val bigDecimals2: Seq[BigDecimal]
val avg1 = avg(bigDecimals2)
val avgs = Vector[AveragedValue](avg0, avg1)
val avgSum = sumAverageValues(avgs)
```
###### AveragedValue of a sequence of AveragedValues

```scala
val falsePositivepProb: Double = 0.01
val words = readWords(wordsPath)
val wordsBF = createBF(words, fpProb)
```
###### Create a BloomFilter

```scala
val falsePositivepProb: Double = 0.01
val word = "path"
val inDict = wordsBF.contains(word).isTrue
```
###### Is word in BloomFilter
```scala
val falsePositivepProb: Double = 0.01
val wordsFalseWords: IndexedSeq[String]
val falsePositives = for {
  i <- wordsFalseWords
  if wordsBF.contains(i).isTrue
} yield i
val acceptable = falsePositives.size < words.size * fpProb
```
###### Is BloomFilter's false positive rate acceptable
```scala
val addrs = inetAddresses(ipRange)
val longZips = inetToLongZip(addrs)
val longs = testLongs(longZips)
implicit val m = createCMSMonoid[Long]()
val cms = createCountMinSketch(longs)
val estimatedCount = cms.totalCount
```
###### CountMinSketch estimate total number of elements seen so far
```scala
val estFreq = cms.frequency(longZips(5))
```
###### CountMinSketch estimate count of elements with the same value as the one selected
```scala
val cms1 = createCountMinSketch(longs)
val cmss = Vector(cms, cms1)
val cmsSum = sumCountMinSketch(cmss)
val estimatedCount = cmsSum.totalCount
```
###### Sum a Sequence of CountMinSketch then estimate combined total number of elements
```scala
val estFreq = cmsSum.frequency(longZips(5))
```
###### From a Sequence of CountMinSketch estimate count of elements with the indexed same value

```scala
val sines = genSineWave(100, 0 to 360)
val days = Range.Double(0.0, 361.0, 1.0)
val sinesZip = sines.zip(days)
val decayedValues = toDecayedValues(sinesZip, 10.0, None)
val avgAt90 = decayedValues(90).average(10.0)
```
###### DecayedValue moving average from the initial value to specified index
```scala
val avg80to90 = decayedValues(90).averageFrom(10.0, 80.0, 90.0)
```
###### DecayedValue moving average from specified index to specified index
```scala
implicit val ag = HyperLogLogAggregator(12)
val ints: Seq[Int]
val hll = createHLL(ints)
```
###### HyperLogLog create a HLL from a sequence of Int
```scala
val hlls = Vector(hll, hll2)
val sum = hlls.reduce(_ + _)
val size = sum.estimatedSize
```
###### Sum a Sequence of HLL and estimate total size
```scala
val approxs = mapHLL2Approximate(hlls)
```
###### Create a sequence of Approximate HHL approximate. Map a sequence of HLL to a sequence of Approximate
```scala
val sum = approxs.reduce(_ + _)
```
###### Sum a Sequence of Approximate and estimate total size
```scala
val level = 5
implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](level)
val qtBD = buildQTree[BigDecimal](bigDecimals)
```
###### Build QTree from a Sequence
```scala
val iqm = qtBD.interQuartileMean
```
###### Get its InterQuartileMean
```scala
val qTrees = Vector(qtBD, qtBD2)
val sumQTree = sumQTrees(qTrees)
```
###### Sum a Sequence of QTrees to a QTree
```scala
def wrapMax[Int](x: Int) = Max(x)
val wm = SeqFunctor.map[Int, Max[Int]](List(1,2,3,4))(wrapMax)
val bigDecimals: Seq[BigDecimal]
val negBigDecimals = SeqFunctor.map[BigDecimal, BigDecimal](bigDecimals)(negate)
val invertedBigDecimals = SeqFunctor.map[BigDecimal, BigDecimal](bigDecimals)(inverse)
```
###### SeqFunctor map elements of a sequence to elements of another sequence.
```scala
val bigDecimals: Seq[BigDecimal]
val invertedNegBigDecimals = andThen[BigDecimal, BigDecimal, BigDecimal](bigDecimals)( inverse)( negate)
```
###### andThen Functor map the elements of a sequence, map that sequence: f() andThen g().
```scala
val bigDecimals: Seq[BigDecimal]
val max = max(bigDecimals)
```
###### Get Max element of a sequence. For Sequence types that have a Semigroup, Monoid and Ordering
```scala
val bigDecimals: Seq[BigDecimal]
val min = min(bigDecimals)
val optBigDecs: [Option[BigDecimal]]
val min2 = min(optBigDecs.flatten)
val eithBigInts = Seq[Either[String, BigInt]]
val min3 = min(filterRight(eithBigInts)
```
###### Get Min element of a sequence.
