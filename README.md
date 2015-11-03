# dendrites

A library to concurently receive and aggregate messages and services for Scala and Akka that is asynchronous, non-blocking, and controls back pressure to prevent data loss from overflow.

Restful clients and servers using
[Akka HTTP](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0/scala/http/index.html) with Scala case class <i class="fa fa-arrows-h"/></i> Json
marshalling/unmarshalling using [spray-json](https://github.com/spray/spray-json). Results from parallel calls are aggregated asynchronously using reuseable [Akka Streams](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0/scala/stream-index.html) components. 

Messages to and from [Akka Actors](http://doc.akka.io/docs/akka/current/scala.html) can be aggregated with a reusable version of the [Akka Aggregator Pattern](http://doc.akka.io/docs/akka/snapshot/contrib/aggregator.html).

Results from a mix of actors and Rest services can be aggregated together.

Statistical approximations of these results are derived using [Twitter Algebird](https://github.com/twitter/algebird) for [Averaged Value](http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm), [Count-min sketch](https://en.wikipedia.org/wiki/Count–min_sketch),
[Decayed Value](https://github.com/twitter/algebird/wiki/Using-DecayedValue-as-moving-average), [HyperLogLog](https://en.wikipedia.org/wiki/HyperLogLog), and [QTree](https://github.com/twitter/algebird/wiki/QTree)
are accumulated in [Akka Agents](http://doc.akka.io/docs/akka/snapshot/scala/agents.html) which can be read and updated by these clients safely and concurrently.

This month I'm finishing the first cut and making it presentable.