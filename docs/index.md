# dendrites

{% include nav.html %}

<img src="png/CQRS.png?raw=true" width="80%" />

Cut down learning curves and custom coding putting together these new generation technologies. Streaming components served together and à la carte, [composable](http://doc.akka.io/docs/akka/current/scala/stream/stream-composition.html#basics-of-composition-and-modularity){:target="_blank"} to [Microservices](http://www.oreilly.com/programming/free/reactive-microservices-architecture-orm.csp){:target="_blank"}, [CRQS](https://martinfowler.com/bliki/CQRS.html){:target="_blank"}, [Event Sourcing](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/){:target="_blank"}, Event Logging and message-driven systems. They're container neutral, deployable to anything that runs on a JVM: Docker, Mesophere, Scala or Java applications etc.

Software should be easy to modify throughout its lifecycle, be resilient in production, and make the most of multi-core CPUs.

Scala's functional and concurrent programming features are made for multi-core CPUs and writing concise, modifiable code.

[Akka Streams](http://doc.akka.io/docs/akka/current/scala/stream/index.html){:target="_blank"} are [Reactive](http://www.reactivemanifesto.org){:target="_blank"}. They use [backpressure](http://doc.akka.io/docs/akka/current/scala/stream/stream-flows-and-basics.html#back-pressure-explained){:target="_blank"} so fast producers don't overflow slow consumers. Stream stages are a nice new form of encapsulation. They're pluggable, debuggable and testable. Underneath they're [Actors](http://doc.akka.io/docs/akka/current/scala/actors.html){:target="_blank"} but it's a nicer programming model: write functions, unit test, pass them to stages, plug stages into streams, then integration test.

Stream stages may need additional objects, values, or functions that don't change while the stream is running. Scala's [currying and partially applied functions](http://alvinalexander.com/scala/fp-book/partially-applied-functions-currying-in-scala){:target="_blank"} and [implicit parameters](http://baddotrobot.com/blog/2015/07/03/scala-implicit-parameters/){:target="_blank"} let generic stages, like `map`, which expect just one input, work with functions having several parameters.

[Custom stages](http://doc.akka.io/docs/akka/current/scala/stream/stream-customize.html){:target="_blank"} are necessary to process functions with contextual information. Some need another object. Others need state. Logging is vital to knowing what happens in asynchronous code, handling results in-stage allows log messages to have input, output, and context information. Errors in distributed systems are often temporary, several *dendrites* stages retry these with [exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff){:target="_blank"}. Other errors have enhanced error and log messages. Kafka polling and Cassandra queries return collections, custom stages input these and output one element at a time when pulled from downstream.

Generic fan-out and fan-in stages make for an elegant way to call parallel services  and proceed when results are ready. 

<img src="png/ParallelHttpFlow.png?raw=true" width="60%"/>

###### Stream segment with 1 input calling services in parallel and 1 aggregated output

A Runnable stream can be passed around as an object.

Programing teams short of experienced Scala developers can be productive and confident. Those new to Scala can start with stand-alone functions then climb a skill ladder, learning progressively instead of being overwhelmed by too many new concepts up front as they would be coding monolithic applications.

Stream stages are simple to modify: most are just a few lines of code and changing one doesn't trigger a cascade of other changes. They can be opinionated, and [YAGNI](http://xp.c2.com/YouArentGonnaNeedIt.html){:target="_blank"}, they don't have to bloat in anticipation of what users might want. Pre-built stages can be used as templates for creating new ones.

Stages are looser than loosely coupled, adjoining ones must only agree on input/output type. This frees developers to swap and modify.  

[Kafka](https://engineering.linkedin.com/kafka/benchmarking-apache-kafka-2-million-writes-second-three-cheap-machines){:target="_blank"} is a fast durable hub between systems and doesn't overflow when downstream systems go down. Wrapping a [Source and a Sink](http://doc.akka.io/docs/akka/current/scala/stream/stream-flows-and-basics.html#defining-and-running-streams){:target="_blank"} around Kafka’s built-in Java Driver integrates it with Akka Streams. Backpressure is repurposed to ensure a stream successfully processed Kafka messages.

[Cassandra](https://academy.datastax.com/resources/brief-introduction-apache-cassandra){:target="_blank"} is a popular highly scalable databases: it's peer to peer with no single point of failure, it elastically adds or removes nodes without restarting, it replicates data to tolerate faults. *dendrites* wraps [DataStax Java Driver](http://docs.datastax.com/en/developer/java-driver/3.3/){:target="_blank"}. Stand-alone functions configure and create a cluster, session, keyspaces, tables, and pre-parse PreparedStatments. Flows bind stream data to PreparedStatements, query, and use Cassandra's [Lightweight Transactions](http://docs.datastax.com/en/cassandra/2.1/cassandra/dml/dml_ltwt_transaction_c.html){:target="_blank"} to update. Sinks insert, delete, and handle user defined compound statements.

Distributed databases like Cassandra aren't suited to the traditional Request/Response model, like Oracle is: writes are fast but queries over many nodes are slow. CRQS, Event Sourcing and generally separating writes from reads are the preferred way to use distributed databases and streaming fits these models.

[Akka HTTP](https://blog.knoldus.com/2016/08/04/introduction-to-akka-http/){:target="_blank"} client API is wrapped in stand-alone non-blocking request and response handlers and Flow stages: streams can include HTTP clients. Its server side has a beautiful high level domain specific language which is easy to write and understandable by non-programmers.

Spray is now part of Akka HTTP and can marshal case classes to JSON requests and unmarshal responses to case classes with a few lines of code.

Twitter’s [Algebird](https://twitter.github.io/algebird/){:target="_blank"} has featherweight hashing components that provide approximate statistics on streaming data in near real time.
 






