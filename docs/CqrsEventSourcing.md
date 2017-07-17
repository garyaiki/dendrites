### CQRS, Event Sourcing streams

{% include nav.html %}

Build Command Query Responsibility Segregation (CQRS), Event Sourcing, and Event Logging streams out of pre-built stages, stand-alone functions, and your custom command, service, and event.

<img src="png/CQRS.png?raw=true" alt="CQRS" width="80%" height="80%">

[CRQS](https://martinfowler.com/bliki/CQRS.html){:target="_blank"} with [Event Sourcing](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/){:target="_blank"} example. 

A remote system publishes commands to a [Kafka](http://kafka.apache.org/documentation){:target="_blank"} topic. Kafka is language neutral, the publishing system can be written in another language, as long as it publishes messages subscribing systems understand.

A subscribing system polls the topic. Once deserialized, commands can be fanned out to parallel sub-streams, one sends them to your service, the other maps them to events and inserts them into an Event Log. [Backpressure](http://doc.akka.io/docs/akka/current/scala/stream/stream-flows-and-basics.html#Back-pressure_explained){:target="_blank"} prevents overflow and ensures parallel sub-streams pull from upstream when both are ready. If the service fails the event isn't logged and vice versa: [example](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/cmd/stream/ShoppingCartCmdAndEvtSpec.scala){:target="_blank"}. An equivalent stream with service and Event Logging connected in series: [example](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/cmd/stream/ShoppingCartCmdAndEvtSinkSpec.scala){:target="_blank"}.

Kafka recommends [Avro](https://en.wikipedia.org/wiki/Apache_Avro){:target="_blank"} for serialization/deserialization. Two flavors are provided: one uses [Avro4s](https://github.com/sksamuel/avro4s){:target="_blank"} which can usually do almost all of it for you, the other offers more control using your Avro schema with a UDF.

KafkaSink configures and wraps Kafka's driver which asynchronously sends messages to your topic.

KafkaSource also configures and wraps the driver. It polls your topic and commits messages.

ConsumerRecordsDequeue pushes one message at a time downstream. It only pulls when all messages polled have been processed.

ConsumerRecordDeserializer maps Avro serialized messages to case classes and pushes them with Kafka's ConsumerRecordMetadata.

Broadcast is a built-in Akka Streams fan out stage, it pushes one input to two outputs, one for command processing the other for Event Logging.

Command processing pushes only the value part of the key/value tuple to a service.

CassandraRetrySink executes commands with a UDF. It can handle different commands to insert, conditional update, or delete. They return a Java ResultSetFuture. The sink configures and wraps DataStax Cassandra [Java driver](http://docs.datastax.com/en/developer/java-driver-dse/1.2/){:target="_blank"} and handles the Future.

The command executing service can be replaced with whatever you need. You create Sinks either by passing a function to the built-in Akka Streams Sink or use one from *dendrites* as a template to write a custom one.

Event Logging maps the key/command tuple to an event case class. Kafka’s message key is re-purposed as the event id, it can also  be used later to find and compensate for duplicates.

CassandraBind creates a BoundStatement from the event case class and a PreparedStatement.

CassandraSink inserts the event into an Event Log.

Duplicates happen when some commands are processed and an error stops processing the rest. Re-polling will process all commands but the ones that succeeded are re-executed. Later on the query side, the 'Q' in CQRS, we can query for events that were logged more than once. From this, a compensating function can restore the correct state ([example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/event/package.scala){:target="_blank"}). After checking for and compensating for duplicates the system is ready for user queries. Try to design [idempotent](https://stackoverflow.com/questions/1077412/what-is-an-idempotent-operation){:target="_blank"} commands, where duplicates can be ignored.

Querying Cassandra clusters can be slow when it has many nodes. Querying can be sped up with composite keys, a Partition Key narrows queries by node, a Clustering Key narrows queries within nodes. A timestamp Clustering Key narrows search by time, useful when you're only concerned with recent events.

The Query side of CQRS isn’t shown, for the example it’s just a Cassandra query of the command table.