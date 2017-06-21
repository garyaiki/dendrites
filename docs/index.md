# dendrites

Looser than loosely coupled streaming components for Microservices, Event Sourcing, Event Logging, Command Query Responsibility Segregation (CQRS), and Reactive systems.
{% include nav.html %}

<img src="png/CQRS.png?raw=true" width="80%" />


Software should be easy to modify throughout its lifecycle, be resilient, and make the most of multi-core CPUs. But seldom is.

Scala's functional and concurrent programming features are made for multi-core CPUs and writing concise, modifiable code.

[Akka Streams](http://doc.akka.io/docs/akka/current/scala/stream/index.html){:target="_blank"} are [Reactive](http://www.reactivemanifesto.org){:target="_blank"} and made for multi-core. They manage backpressure so fast producers don't overflow slow consumers. Stream stages are a fit for purpose form of encapsulation. They're pluggable, debuggable and testable. Underneath they're Actors but it's a nicer programming model: write functions, unit test, pass them to stages, plug stages into streams, then integration test. A Runnable stream can be passed around as an object. Asynchronous, concurrent programming is often called **HARD** as in, *too* hard. Akka Streams, once learned, are easily preferable to the conventional way.

Akka Stream’s fan-out and fan-in stages make for an elegant way to call external services in parallel and proceed when results are ready. 

<img src="png/ParallelHttpFlow.png?raw=true" width="60%"/>

###### Stream segment with 1 input calling services in parallel and 1 aggregated output
Programing teams short of experienced Scala developers can be productive and confident. Those new to Scala can start with stand-alone functions then climb a skill ladder, learning progressively instead of being overwhelmed by too many new concepts up front as they would be coding a monolithic application.

Stream stages are simple to modify: most are just a few lines of code and changing a stage doesn't trigger a cascade of other changes. Stages can be small, opinionated, and [YAGNI](http://xp.c2.com/YouArentGonnaNeedIt.html){:target="_blank"}, they don't have to bloat trying to anticipate what users might want. *dendrites* pre-built stages can be customized without needing to understand more than a few lines of code. 

[Kafka](https://engineering.linkedin.com/kafka/benchmarking-apache-kafka-2-million-writes-second-three-cheap-machines){:target="_blank"} integrates with Akka Streams by wrapping a [Source and a Sink](http://doc.akka.io/docs/akka/current/scala/stream/stream-flows-and-basics.html#defining-and-running-streams){:target="_blank"} around Kafka’s built-in Java Driver. Kafka is a fast durable hub between heterogenous systems, and a buffer that doesn't overflow when downstream systems go down. [Backpressure](http://www.reactivemanifesto.org/glossary#Back-Pressure){:target="_blank"} is used to ensure a stream successfully processed messages polled by Kafka.

[Cassandra](https://academy.datastax.com/resources/brief-introduction-apache-cassandra){:target="_blank"} integrates with Akka Streams by wrapping a Flow or Sink around Cassandra’s Java Driver. BoundStatements make for generic Cassandra stream stages.

Distributed databases like Cassandra aren't suited to the traditional Request/Response model, like Oracle is. [CRQS](https://martinfowler.com/bliki/CQRS.html){:target="_blank"}, [Event Sourcing](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/){:target="_blank"} and generally separating writes from reads are the preferred way to use distributed databases and streaming fits these models.

[Akka HTTP](https://blog.knoldus.com/2016/08/04/introduction-to-akka-http/){:target="_blank"} has a beautiful high level domain specific language for servers which are easy to write and understandable to non-programmers. Its client API enables non-blocking client request and response handlers to be composed from mostly generic functions and flow stages.

Spray is now part of Akka HTTP and can marshal case classes to JSON Http requests and unmarshal Http responses to case classes with a few lines of code.

Twitter’s [Algebird](https://twitter.github.io/algebird/){:target="_blank"} has featherweight hashing components that provide approximate statistics on streaming data in near real time.

*dendrites* isn't a framework. It's designed for you use its functions with or without Akka Streams, its streaming stages à la carte, to use any, none, or all of its components, and to use them as is or as starting points for custom ones. 




