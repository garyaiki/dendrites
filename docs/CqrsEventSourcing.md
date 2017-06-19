### CQRS, Event Sourcing streaming components

{% include nav.html %}
Build Command Query Responsibility Segregation streams out of, mostly, [pre-built Akka Stream stages](https://github.com/garyaiki/dendrites/wiki/PreBuiltStreamStages){:target="_blank"}, functions, and your custom code.

<img src="png/CQRS.png?raw=true" alt="CQRS" width="100%" height="100%">

[CRQS](https://martinfowler.com/bliki/CQRS.html){:target="_blank"} with [Event Sourcing](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/){:target="_blank"} example. 

A remote system publishes commands to a [Kafka](http://kafka.apache.org/documentation){:target="_blank"} topic. Kafka is language neutral, the command publishing system's only constraint is that it publishes messages its subscribing systems understand.

A subscribing system polls the topic. Once deserialized, commands are fanned out to parallel sub-streams, one sends them to your service, the other maps them to events and inserts them into an Event Log. [Backpressure](http://doc.akka.io/docs/akka/current/scala/stream/stream-flows-and-basics.html#Back-pressure_explained){:target="_blank"} prevents overflow and ensures parallel sub-streams pull from upstream when both are ready. If the service fails the event won't be logged and vice versa. [Example code](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/cmd/stream/ShoppingCartCmdAndEvtSpec.scala){:target="_blank"}

Equivalent code with the service and Event Logging in series is [here](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/cmd/stream/ShoppingCartCmdAndEvtSinkSpec.scala){:target="_blank"}.

Kafka recommends [Avro](https://en.wikipedia.org/wiki/Apache_Avro){:target="_blank"}  serialization. Two serialization stages are provided: [Avro4s](https://github.com/sksamuel/avro4s){:target="_blank"} does almost all of it for you, or an Avro schema with a UDF maps an Avro GenericRecord to your case class.

KafkaSink configures and wraps Kafka's driver and asynchronously sends messages to your topic.

KafkaSource also configures and wraps the driver. It polls your topic for published  messages.

ConsumerRecordsDequeue sends one message at a time downstream. It only pulls when all messages polled have been processed.

ConsumerRecordDeserializer turns Avro serialized messages into case classes and pushes them with their ConsumerRecord key.

Broadcast is a built-in Akka Stream fan out stage, it pushes one input to two outputs, one for command processing the other for Event Logging.

Command processing pushes only the value part of the key/value tuple to a service.

In this example CassandraRetrySink executes commands. It configures and wraps [DataStax' Cassandra Java driver](http://docs.datastax.com/en/developer/java-driver-dse/1.2/){:target="_blank"}. You construct the sink with a function that executes insert, conditional update, or delete statements and returns a Java ResultSetFuture. The sink handles the Future and retries retry-able errors.

The service can be whatever you need. You can write custom Sinks for your services by passing a function to the built-in Akka Stream Sink or write a custom Sink. You can use code from one of *dendrites* Sinks as a starting point.

Event Logging maps the key/value tuple to a case class. Kafka’s message key is re-purposed as the event id, this is used later to find and compensate for duplicates.

CassandraBind creates a BoundStatement from the event's case class and a PreparedStatement.

CassandraSink inserts the event into an Event Log.

Duplicates happen when some commands are processed and an error stops processing the rest. Re-polling will process all commands but the ones that succeeded are re-executed. So far, we've only done the Command side. Later on the query side, we can query to find events that were executed more than once. From this, a compensating function can restore the correct state ([example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/event/package.scala){:target="_blank"}). After checking for and compensating for duplicates CQRS is ready for user queries.

Querying Cassandra clusters can be slow when it has many nodes. Querying the Event Log can be sped up with a composite key, a Partition Key narrows queries by node, a Clustering Key narrows queries within nodes. A timestamp Clustering Key narrows search by time, useful when you're only concerned with recent events.

The Query side of CQRS isn’t shown, for the example it’s just a Cassandra query of the command table.