
# dendrites

A Scala library of looser than loosely coupled streaming components for Microservices, Event Sourcing, Event Logging, Command Query Responsibility Segregation (CQRS), scaling up, and scaling out. Akka Streams stages and stand-alone functions for Kafka, Avro, Cassandra, Algebird, Akka HTTP, and Akka Actors. 
<p >
<img src="https://github.com/garyaiki/dendrites/blob/master/docs/png/CQRS.png?raw=true" width="50%" />
</p>

Software should make the most of multi-core CPUs, be easy to modify throughout its lifecycle, and be resilient in an ensemble of systems. Today's software is usually the opposite because metamorphosis is hard. Adapting popular core technologies to streaming, functional programming, and disentangling common from custom code can turn a blocking, synchronous spaghetti code caterpillar into a [Reactive](http://www.reactivemanifesto.org){:target="_blank"} butterfly.

Scala's functional and concurrent programming features are made for multi-core CPUs. Functional programming can keep software clean, modifiable, and reduce custom code.

[Akka Streams](http://doc.akka.io/docs/akka/current/scala/stream/index.html){:target="_blank"} are also ideal for multi-core. They manage backpressure so fast producers don't overflow slow consumers. Stream stages provide a fit for purpose form of encapsulation. They're debuggable and testable. They are Actors underneath but it's a nicer programming model: write functions, unit test, plug them into stages, plug stages into streams, then integration test. Runnable streams can be passed around as an object. Asynchronous, concurrent programming is no longer forbidding.

Akka Stream’s fan-out and fan-in stages make for an elegant way to call external services in parallel and proceed when results are ready. 
<p >
<img src="https://github.com/garyaiki/dendrites/blob/master/docs/png/ParallelHttpFlow.png?raw=true" width="50%"/>
</p>

Programing teams short of experienced Scala developers can be productive and confident. Those new to Scala can climb a skill ladder starting with stand-alone functions then learn progressively instead of being overwhelmed by too many new concepts up front as they would be coding a monolithic application.

Stream stages are simple to modify: they don’t have much code and changing one doesn't force a cascade of other changes. Stages are small enough to be opinionated and [YAGNI](http://xp.c2.com/YouArentGonnaNeedIt.html){:target="_blank"}, they don't have to bloat anticipating what users might want. *dendrites* pre-built stages can be customized without needing to understand more than a few lines of code. 

[Kafka](https://engineering.linkedin.com/kafka/benchmarking-apache-kafka-2-million-writes-second-three-cheap-machines){:target="_blank"} integrates with Akka Streams by wrapping Kafka’s Java Driver as a Source and as a Sink. They connect to Kafka Brokers which are a fast durable hub between heterogenous systems, and a buffer that doesn't overflow. [Backpressure](http://www.reactivemanifesto.org/glossary#Back-Pressure){:target="_blank"} is used to ensure a stream has successfully processed messages polled by Kafka.

[Kafka, Avro streaming page](KafkaAvro.html)

[Cassandra](https://academy.datastax.com/resources/brief-introduction-apache-cassandra){:target="_blank"} integrates with Akka Streams by wrapping Cassandra’s Java Driver. Cassandra's BoundStatement gives streams a generic way to call Cassandra from Flow or Sink stages.

[Cassandra streaming page](Cassandra.html)

[Akka HTTP](https://blog.knoldus.com/2016/08/04/introduction-to-akka-http/){:target="_blank"} has a beautiful high level domain specific language for servers which are easy to write and understandable to non-programmers. Its client API enables non-blocking client request and response handlers to be composed from mostly generic functions and flow stages.

Spray is now part of Akka HTTP and can marshal case classes to JSON Http requests and unmarshal Http responses to case classes with a few lines of code.

[Akka HTTP streaming with JSON page](HttpJson.html)

Twitter’s [Algebird](https://twitter.github.io/algebird/){:target="_blank"} has featherweight hashing components that provide approximate statistics on streaming data in near real time.

[Algebird hashing statistics page](TwitterAlgebird.html)

##### Akka Streams with Akka Actors
[Streams with Actors page](StreamsWithActors.html)


