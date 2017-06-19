### CQRS, Event Sourcing streaming components

{% include nav.html %}
Build Command Query Responsibility Segregation streams out of, mostly, [pre-built Akka Stream stages](https://github.com/garyaiki/dendrites/wiki/PreBuiltStreamStages){:target="_blank"}, functions, and your custom code.

<img src="png/CQRS.png?raw=true" alt="CQRS" width="100%" height="100%">

[CRQS](https://martinfowler.com/bliki/CQRS.html){:target="_blank"} with [Event Sourcing](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/){:target="_blank"} example. 

A remote system sends commands to [Kafka](http://kafka.apache.org/documentation){:target="_blank"}. A processing system polls it. Commands are fanned out to parallel sub-streams, one sends commands to your service, the other maps commands to events and inserts them into an Event Log. [Backpressure](http://doc.akka.io/docs/akka/current/scala/stream/stream-flows-and-basics.html#Back-pressure_explained){:target="_blank"} prevents overflow and ensures parallel sub-streams pull from upstream when both are ready. If the service fails the event won't be logged and vice versa. [Example code](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/cmd/stream/ShoppingCartCmdAndEvtSpec.scala){:target="_blank"}

Equivalent code with the service and Event Logging in series is [here](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/cmd/stream/ShoppingCartCmdAndEvtSinkSpec.scala){:target="_blank"}.

Kafka recommends [Avro](https://en.wikipedia.org/wiki/Apache_Avro){:target="_blank"}  serialization. Two serialization stages are provided: [Avro4s](https://github.com/sksamuel/avro4s){:target="_blank"} does almost all of it for you, or an Avro schema with a UDF maps an Avro GenericRecord to your case class.

Kafka is written in Scala but its built in driver is written in Java. KafkaSink configures and wraps the driver and calls its asynchronous API. It takes care of Java & Scala futures, callbacks and retries Kafka's [RetriableExceptions](http://kafka.apache.org/0102/javadoc/index.html?org/apache/kafka/common/errors/RetriableException.html){:target="_blank"}.

KafkaSource also configures and wraps the driver. It polls Kafka for  messages, this is a blocking call so KafkaSource is passed a blocking ExecutionContext (Akka Stream’s [default ExecutionContext](http://doc.akka.io/docs/akka/2.5.1/scala/dispatchers.html#default-dispatcher){:target="_blank"} has about as many threads as cpu cores, blocking could cause thread starvation). The driver returns messages in a ConsumerRecords object which the sink pushes to the next stage.

Kafka tracks messages with offsets. Committing a message means its offset is marked as committed. Kafka auto-commits messages by default but this only assures they were read OK. By turning off auto-commit we can do it after processing, assuring they were read *and processed* OK. If any failed, Kafka re-polls those messages.

ConsumerRecordsDequeue initially takes every ConsumerRecord contained in ConsumerRecords and queues them. Then it pushes one ConsumerRecord for each downstream pull. If the queue is not empty it doesn't pull from upstream. This is to ensure all messages have been processed. After the queue empties it resumes pulling, causing KafkaSource to commit its previous poll and poll for new messages.

ConsumerRecordDeserializer takes a UDF ([example with Avro4s](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/avro4s/Avro4sGetAccountBalances.scala){:target="_blank"}) to deserialize from an Array of Bytes to a case class. An alternate ConsumerRecordDeserializer  takes an Avro schema and maps to a GenericRecord, which is in turn mapped by a UDF to a case class. Either way a deserialized case class and the ConsumerRecord's key is pushed.

Broadcast is a built-in Akka Stream fan out stage, it pushes one input to two outputs, one for command processing the other for Event Logging.

Command processing pushes only the value part of the key/value tuple to a service.

In the example CassandraRetrySink executes commands. It configures and wraps [DataStax' Cassandra Java driver](http://docs.datastax.com/en/developer/java-driver-dse/1.2/){:target="_blank"}. You construct the sink with a function that executes insert, conditional update, or delete statements and returns a Java ResultSetFuture. The sink handles the Future and retries retry-able errors.

You can write custom Sinks for your services by passing a function to the built-in Akka Stream Sink or write a custom Sink. You can use one of the *dendrites* Sinks as a starting point.

Event Logging maps the key/value tuple to a case class. Kafka’s message key is re-purposed as the event id, this is used later to find and compensate for duplicates.

CassandraBind creates a BoundStatement from the event's case class and a PreparedStatement.

CassandraSink inserts the event into an Event Log.

Duplicates happen when some commands are processed and an error stops processing the rest. Re-polling will process all commands but the ones that succeeded are re-executed. So far, we've only done the Command side. Later on the query side, we can query to find events that were executed more than once. From this, a compensating function can restore the correct state ([example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/event/package.scala){:target="_blank"}). After checking for and compensating for duplicates CQRS is ready for user queries.

Querying Cassandra clusters can be slow when it has many nodes. Querying the Event Log can be sped up with a composite key, a Partition Key narrows queries by node, a Clustering Key narrows queries within nodes. A timestamp Clustering Key narrows search by time, useful when you're only concerned with recent events.

The Query side of CQRS isn’t shown, for the example it’s just a Cassandra query of the command table.