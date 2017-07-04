### CQRS, Event Sourcing streams

{% include nav.html %}

Build Command Query Responsibility Segregation streams out of, stand-alone functions, pre-built stream stages, and your custom code.

<img src="png/CQRS.png?raw=true" alt="CQRS" width="80%" height="80%">

[CRQS](https://martinfowler.com/bliki/CQRS.html){:target="_blank"} with [Event Sourcing](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/){:target="_blank"} example. 

A remote system publishes commands to a [Kafka](http://kafka.apache.org/documentation){:target="_blank"} topic. Kafka is language neutral, the command publishing system's only constraint is that it publishes messages subscribing systems understand.

A subscribing system polls the topic. Once deserialized, commands can be fanned out to parallel sub-streams, one sends them to your service, the other maps them to events and inserts them into an Event Log. [Backpressure](http://doc.akka.io/docs/akka/current/scala/stream/stream-flows-and-basics.html#Back-pressure_explained){:target="_blank"} prevents overflow and ensures parallel sub-streams pull from upstream when both are ready. If the service fails the event isn't logged and vice versa: [example](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/cmd/stream/ShoppingCartCmdAndEvtSpec.scala){:target="_blank"}. An equivalent stream with service and Event Logging in series [here](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/cmd/stream/ShoppingCartCmdAndEvtSinkSpec.scala){:target="_blank"}.

Kafka recommends [Avro](https://en.wikipedia.org/wiki/Apache_Avro){:target="_blank"}  serialization. Two serialization stages are provided: [Avro4s](https://github.com/sksamuel/avro4s){:target="_blank"} does almost all of it for you, or an Avro schema with a UDF maps an Avro GenericRecord to your case class.

KafkaSink configures and wraps Kafka's driver which asynchronously sends messages to your topic.

KafkaSource also configures and wraps the driver. It polls your topic.

ConsumerRecordsDequeue pushes one message at a time downstream. It only pulls when all messages polled have been processed.

ConsumerRecordDeserializer maps Avro serialized messages to case classes and pushes them with Kafka's ConsumerRecordMetadata.

Broadcast is a built-in Akka Stream fan out stage, it pushes one input to two outputs, one for command processing the other for Event Logging.

Command processing pushes only the value part of the key/value tuple to a service.

In this example CassandraRetrySink executes commands. It configures and wraps DataStax Cassandra [Java driver](http://docs.datastax.com/en/developer/java-driver-dse/1.2/){:target="_blank"}. You construct the sink with a function that executes insert, conditional update, or delete statements and returns a Java ResultSetFuture. The sink handles the Future.

The service can be whatever you need. You write custom Sinks for services by passing a function to the built-in Akka Stream Sink or write a custom Sink using a *dendrites* Sink as a starting place.

Event Logging maps the key/value tuple to a case class. Kafka’s message key is re-purposed as the event id, this is used later to find and compensate for duplicates.

CassandraBind creates a BoundStatement from the event's case class and a PreparedStatement.

CassandraSink inserts the event into an Event Log.

Duplicates happen when some commands are processed and an error stops processing the rest. Re-polling will process all commands but the ones that succeeded are re-executed. So far, we've only done the Command side. Later on the query side, we can query to find events that were executed more than once. From this, a compensating function can restore the correct state ([example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/event/package.scala){:target="_blank"}). After checking for and compensating for duplicates CQRS is ready for user queries. Try to design [idempotent](https://stackoverflow.com/questions/1077412/what-is-an-idempotent-operation){:target="_blank"} operations, where duplicates can be ignored.

Querying Cassandra clusters can be slow when it has many nodes. Querying can be sped up with composite keys, a Partition Key narrows queries by node, a Clustering Key narrows queries within nodes. A timestamp Clustering Key narrows search by time, useful when you're only concerned with recent events.

The Query side of CQRS isn’t shown, for the example it’s just a Cassandra query of the command table.