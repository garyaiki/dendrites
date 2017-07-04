# dendrites

Looser than loosely coupled streaming components for Microservices, Event Sourcing, Event Logging, Command Query Responsibility Segregation (CQRS), Reactive systems, and the ACK in a [SMACK stack](https://mesosphere.com/blog/2017/06/21/smack-stack-new-lamp-stack/){:target="_blank"}.

{% include nav.html %}

<img src="png/CQRS.png?raw=true" width="80%" />

*dendrites* tackles challenges presented by these new generation technologies and cuts down the learning curve and custom coding.

Software should be easy to modify throughout its lifecycle, be resilient, and make the most of multi-core CPUs. But seldom is.

Scala's functional and concurrent programming features are ideal for multi-core CPUs and writing concise, modifiable code.

[Akka Streams](http://doc.akka.io/docs/akka/current/scala/stream/index.html){:target="_blank"} are [Reactive](http://www.reactivemanifesto.org){:target="_blank"}. They use [backpressure](http://www.reactivemanifesto.org/glossary#Back-Pressure){:target="_blank"} so fast producers don't overflow slow consumers. Stream stages are a nice new form of encapsulation. They're pluggable, debuggable and testable. Underneath they're Actors but it's a nicer programming model: write functions, unit test, pass them to stages, plug stages into streams, then integration test. A Runnable stream can be passed around as an object. Asynchronous, concurrent programming is often called **HARD** as in, *too* hard. Akka Streams, once learned, are easily preferable to the old ways.

Akka Stream’s fan-out and fan-in stages make for an elegant way to call parallel services  and proceed when results are ready. 

<img src="png/ParallelHttpFlow.png?raw=true" width="60%"/>

###### Stream segment with 1 input calling services in parallel and 1 aggregated output
Programing teams short of experienced Scala developers can be productive and confident. Those new to Scala can start with stand-alone functions then climb a skill ladder, learning progressively instead of being overwhelmed by too many new concepts up front as they would be coding a monolithic application.

Stream stages are simple to modify: most are just a few lines of code and changing one doesn't trigger a cascade of other changes. They can be opinionated, and [YAGNI](http://xp.c2.com/YouArentGonnaNeedIt.html){:target="_blank"}, they don't have to bloat in anticipation of what users might want. Pre-built stages can be used as templates for creating new ones. 

[Kafka](https://engineering.linkedin.com/kafka/benchmarking-apache-kafka-2-million-writes-second-three-cheap-machines){:target="_blank"} is a fast durable hub between heterogenous systems, and a buffer that doesn't overflow when downstream systems go down. Wrapping a [Source and a Sink](http://doc.akka.io/docs/akka/current/scala/stream/stream-flows-and-basics.html#defining-and-running-streams){:target="_blank"} around Kafka’s built-in Java Driver integrates it with Akka Streams. Backpressure is repurposed to ensure a stream successfully processed Kafka messages.

[Cassandra](https://academy.datastax.com/resources/brief-introduction-apache-cassandra){:target="_blank"} is one of the most popular highly scalable databases: it's Peer to Peer with no single point of failure, it elasticly scales up to any number of nodes or down without restarting, it replicates data to tolerate faults. *dendrites* wraps [DataStax Java Driver](http://docs.datastax.com/en/developer/java-driver/3.3/){:target="_blank"}. Stand-alone functions configure and create a cluster, session, keyspaces, tables, and pre-parse PreparedStatments. Flows bind stream data to PreparedStatements, query, and use Cassandra's [Lightweight Transactions](http://docs.datastax.com/en/cassandra/2.1/cassandra/dml/dml_ltwt_transaction_c.html){:target="_blank"} to update. Sinks insert, delete, and handle user defined compound statements.

Distributed databases like Cassandra aren't suited to the traditional Request/Response model, like Oracle is: writes are fast but queries over many nodes are slow. [CRQS](https://martinfowler.com/bliki/CQRS.html){:target="_blank"}, [Event Sourcing](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/){:target="_blank"} and generally separating writes from reads are the preferred way to use distributed databases and streaming fits these models.

[Akka HTTP](https://blog.knoldus.com/2016/08/04/introduction-to-akka-http/){:target="_blank"} client API is wrapped in stand-alone non-blocking request and response handlers and non-blocking Flow stages: streams can include HTTP clients. Its server side has a beautiful high level domain specific language which is easy to write and understandable by non-programmers.

Spray is now part of Akka HTTP and can marshal case classes to JSON requests and unmarshal responses to case classes with a few lines of code.

Twitter’s [Algebird](https://twitter.github.io/algebird/){:target="_blank"} has featherweight hashing components that provide approximate statistics on streaming data in near real time.

*dendrites* isn't a framework. It's designed for you use its functions with or without Akka Streams, its streaming stages à la carte, to use any, none, or all of its components, and to use them as is or as starting points for custom ones. 




