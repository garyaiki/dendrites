Combine Twitter [Algebird](https://github.com/twitter/algebird) approximators with Akka [Agents](http://doc.akka.io/docs/akka/2.4/scala/agents.html)
![image](https://github.com/garyaiki/dendrites/blob/master/docs/png/AlgebirdApproximatorsAgentsFlow.png?raw=true)
[AveragedValue](https://github.com/twitter/algebird/blob/develop/algebird-core/src/main/scala/com/twitter/algebird/AveragedValue.scala), [CountMinSketch](https://github.com/twitter/algebird/blob/develop/algebird-core/src/main/scala/com/twitter/algebird/CountMinSketch.scala), [DecayedValue](https://github.com/twitter/algebird/blob/develop/algebird-core/src/main/scala/com/twitter/algebird/DecayedValue.scala), [HyperLogLog](https://github.com/twitter/algebird/blob/develop/algebird-core/src/main/scala/com/twitter/algebird/HyperLogLog.scala), and [QTree](https://github.com/twitter/algebird/blob/develop/algebird-core/src/main/scala/com/twitter/algebird/QTree.scala) are fast hashing approximators. Agents make these running totals globally readable and updatable.