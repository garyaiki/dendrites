### CQRS, Event Sourcing with Kafka, Avro, Cassandra, and Akka Streams

Build Command Query Responsibility Segregation streams out of, mostly, [pre-built Akka Stream stages](https://github.com/garyaiki/dendrites/wiki/PreBuiltStreamStages){:target="_blank"}, functions, and your custom code.

<img src="png/CQRS.png?raw=true" alt="CQRS" width="100%" height="100%">

[CRQS](https://martinfowler.com/bliki/CQRS.html){:target="_blank"} with [Event Sourcing](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/){:target="_blank"} example. 

A remote system sends commands to [Kafka](http://kafka.apache.org/documentation){:target="_blank"}. A processing system polls it. Commands are fanned out to parallel sub-streams, one sends commands to your service, the other turns commands into event objects and inserts them into a [Cassandra](https://academy.datastax.com/resources/brief-introduction-apache-cassandra){:target="_blank"} Event Log. [Backpressure](http://doc.akka.io/docs/akka/current/scala/stream/stream-flows-and-basics.html#Back-pressure_explained){:target="_blank"} prevents overflow and ensures parallel sub-streams pull from upstream when both are ready. If the service fails the event won't be logged and vice versa. [Example code](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/cmd/stream/ShoppingCartCmdAndEvtSpec.scala){:target="_blank"}

Code with the service and Event Logging in series is [here](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/cmd/stream/ShoppingCartCmdAndEvtSinkSpec.scala){:target="_blank"}.

Shown is a Cassandra pre-built stage can handle insert, [conditional update](https://opencredo.com/new-features-in-cassandra-2-0-lightweight-transactions-on-insert/){:target="_blank"}, and delete.

Kafka recommends [Avro](https://en.wikipedia.org/wiki/Apache_Avro){:target="_blank"}  serialization. Two serialization stages are provided, one where [Avro4s](https://github.com/sksamuel/avro4s){:target="_blank"} does it all for you, the other uses an explicit Avro schema your function that maps an Avro GenericRecord to your case class.

Kafka is written in Scala but its built in driver is written in Java. KafkaSink configures and wraps the Java driver and calls its asynchronous API. KafkaSink takes care of Java & Scala futures, callbacks and it retries Kafka's [RetriableExceptions](http://kafka.apache.org/0102/javadoc/index.html?org/apache/kafka/common/errors/RetriableException.html){:target="_blank"}.

KafkaSource also configures and wraps the Java driver. It polls Kafka for  messages, this is a blocking call so KafkaSource is passed a blocking ExecutionContext (Akka Stream’s [default ExecutionContext](http://doc.akka.io/docs/akka/2.5.1/scala/dispatchers.html#default-dispatcher){:target="_blank"} only has as many threads as the cpu has cores, blocking could cause thread starvation). The driver returns messages as ConsumerRecords which is pushed to the next stage.

Kafka tracks messages with offsets. Committing a message means marking its offset as committed. Kafka doesn’t poll committed messages. Kafka auto-commits messages by default, assuring they were read OK. By turning off auto-commit we can wait until after processing to commit, assuring they were read *and processed* OK. If any failed, Kafka re-polls those messages.

ConsumerRecordsDequeue enqueues records if the queue is empty. If the queue is not empty it dequeues one ConsumerRecord. It waits until the Queue is empty to pull from upstream. This is to ensure all messages have been processed. Pulling after empty causes KafkaSource to commit the previous poll and poll for new messages.

ConsumerRecordDeserializer uses your function (easy with Avro4s) to deserialize from an Array of Bytes to your case class. An alternate ConsumerRecordDeserializer  takes an explicit Avro schema and maps to an Avro GenericRecord, which is mapped by your function to your case class. Either way a deserialized case class and corresponding Kafka key is pushed.

Broadcast is a built-in Akka Stream fan out stage, it pushes input to two outputs, one for command processing and the other for Event Logging. Akka Streams lets us do this in parallel or in series.

The command processing sub-stream starts with a function that pushes only the value part of the key/value tuple.

CassandraRetrySink executes the command. [DataStax provides a Cassandra Java driver](http://docs.datastax.com/en/developer/java-driver-dse/1.2/){:target="_blank"}. CassandraRetrySink configures and wraps the driver. You pass it a function that executes insert, conditional update, or delete statements and returns a Java ResultSetFuture. The sink handles the Future and retries retry-able errors.

You can write custom Sinks for your services by passing a function to the built-in Akka Stream Sink or write a custom Sink. You can use one of the *dendrites* Sinks as a starting point.

The Event Logging sub-stream starts with a function that maps the key/value tuple to an event case class. Kafka’s message key is re-purposed as the event id, this is used later to find and compensate for duplicates.

CassandraBind takes the event case class and a PreparedStatement and creates a BoundStatement.

CassandraSink inserts the event into an Event Log.

Duplicates can occur when some commands are processed and an error prevents processing of the rest. Re-polling will process all commands but the ones that succeeded the first time are re-executed. Later, a query finds events that were executed more than once. From this, a compensating command can be created to restore the correct state.

Querying Cassandra clusters with many nodes can be slow. Querying the Event Log can be sped up with a composite key, a Partition Key narrows queries by node, a Clustering Key narrows queries within nodes. A timestamp Clustering Key narrow the search by time, useful when you're only concerned with recent events.

Not shown is a process that queries the Event log for duplicate Event ids then a custom compensating command is executed. [example code](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/event/package.scala){:target="_blank"}.

The Query side of CQRS isn’t shown, for the example it’s just a Cassandra query of the command table.