### Kafka, Avro streaming components

{% include nav.html %}

Kafka and Avro stream stages.

[<img src="png/KafkaSink.png?raw=true" alt="KafkaSink" width="25%" height="25%" title="input serialized value, publish it, uses Akka Supervision to retry temporary errors with exponential backoff or fail the stream">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/KafkaSink.scala){:target="_blank"}
[<img src="png/KafkaSource.png?raw=true" alt="KafkaSource" width="23%" height="23%" title="input Kafka, output ConsumerRecords">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/KafkaSource.scala){:target="_blank"}
[<img src="png/consumerRecordsDequeue.png?raw=true" alt="consumerRecordsToQequeue" width="21%" height="21%" title="input ConsumerRecords, output ConsumerRecord one at a time">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/ConsumerRecordsToQueue.scala){:target="_blank"}
[<img src="png/dualConsumerRecordsFlow.png?raw=true" alt="dualConsumerRecordsFlow" width="25%" height="25%" title="input ConsumerRecords from 2 partitions, output tuple of Queue of ConsumerRecord with queue for each partition">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/package.scala){:target="_blank"}
[<img src="png/tripleConsumerRecordsFlow.png?raw=true" alt="tripleConsumerRecordsFlow" width="25%" height="25%" title="input ConsumerRecords from 3 partitions, output tuple of Queue of ConsumerRecord with queue for each partition">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/package.scala){:target="_blank"}
[<img src="png/extractValueFlow.png?raw=true" alt="extractValueFlow" width="20%" height="20%" title="input ConsumerRecord, output its value">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/package.scala){:target="_blank"}
[<img src="png/ConsumerRecordDeserializer.png?raw=true" alt="ConsumerRecordDeserializer" width="25%" height="25%" title="input a ConsumerRecord, output a tuple of Kafka key and case class value">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/avro4s/ConsumerRecordDeserializer.scala){:target="_blank"}
[<img src="png/AvroDeserializer.png?raw=true" alt="AvroDeserializer" width="20%" height="20%" title="input an Avro serialized array of bytes, output the case class it maps to">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/avro4s/stream/Avro4sDeserializer.scala){:target="_blank"}
[<img src="png/AvroSerializer.png?raw=true" alt="AvroSerializer" width="20%" height="20%" title="input a case class, output an Avro serialized array of bytes">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/avro4s/stream/Avro4sSerializer.scala){:target="_blank"}
###### Click image to open source code in a new tab. Hover over image for stage inputs and outputs

[Kafka](https://engineering.linkedin.com/kafka/benchmarking-apache-kafka-2-million-writes-second-three-cheap-machines){:target="_blank"} is the preferred distributed system hub for [microservices](https://martinfowler.com/articles/microservices.html){:target="_blank"}, [CRQS](https://martinfowler.com/bliki/CQRS.html){:target="_blank"}, [Event Sourcing](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/){:target="_blank"} and messaging. Most Kafka client coding is generalized and it's enhanced with  [backpressure](http://www.reactivemanifesto.org/glossary#Back-Pressure){:target="_blank"}, in-stage error handling, and reliable message processing.


#### Kafka as a stream source

<img src="png/KafkaSourceStream.png?raw=true" width="50%" />

###### Common stages of streams beginning with a KafkaSource, downstream can be anything

Poll messages from a cluster, queue them, deserialize them, and commit them once processed.

Kafka is written in Scala but its built in driver is written in Java.

KafkaSource wraps the driver and configures a [KafkaConsumer](http://kafka.apache.org/0110/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html){:target="_blank"} which polls and commits messages. These are rare blocking calls in *dendrites*, so a blocking ExecutionContext is used (Akka Stream’s [default ExecutionContext](http://doc.akka.io/docs/akka/current/scala/dispatchers.html#default-dispatcher){:target="_blank"} has about as many threads as cpu cores, blocking could cause thread starvation). Available messages are returned in a [ConsumerRecords](http://kafka.apache.org/0110/javadoc/org/apache/kafka/clients/consumer/ConsumerRecords.html){:target="_blank"} object. If either polling or committing throws a [RetriableExceptions](http://kafka.apache.org/0110/javadoc/index.html?org/apache/kafka/common/errors/RetriableException.html){:target="_blank"} it's retried with [exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff){:target="_blank"} delays.

Kafka tracks messages with offsets. Committing a message means its offset is marked as committed. Kafka auto-commits by default but this only assures they were read OK. By turning off auto-commit we wait until they were processed. If any failed, Kafka re-polls those messages.

A key benefit of Akka is [Supervision](http://doc.akka.io/docs/akka/current/scala/stream/stream-error.html){:target="_blank"} for error handling and recovery, its default behavior is stopping the stream when an exception is thrown. Kafka throws retriable exceptions so KafkaSource has a [Decider](http://doc.akka.io/docs/akka/current/scala/stream/stream-error.html){:target="_blank"} to handle them. KafkaSource doesn’t commit offsets in a failed stream so records will be polled again. Downstream stages throwing exceptions mustn't Restart or Resume: a failed message could get through.

ConsumerRecordsToQueue initially queues every [ConsumerRecord](http://kafka.apache.org/0110/javadoc/org/apache/kafka/clients/consumer/ConsumerRecord.html){:target="_blank"} contained in ConsumerRecords. Then it pushes one for each downstream pull. If the queue is not empty it doesn't pull from upstream. This is to ensure all messages have been processed. After the queue empties it resumes pulling, causing KafkaSource to commit its previous poll and poll anew. 

ConsumerRecordDeserializer maps a ConsumerRecord's value to a case class and its metadata to a [ConsumerRecordMetadata](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/package.scala){:target="_blank"}. There're two flavors of ConsumerRecordDeserializer, one takes a UDF ([example with Avro4s](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/avro4s/Avro4sGetAccountBalances.scala){:target="_blank"}) to deserialize from an Array of Bytes to a case class, the other takes an Avro schema and maps to a [GenericRecord](http://avro.apache.org/docs/current/api/java/org/apache/avro/generic/GenericRecord.html){:target="_blank"}, which is in turn mapped by a UDF to a case class
 ([example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/avro/package.scala){:target="_blank"}). Either way a deserialized case class and ConsumerRecordMetadata is pushed.

ConsumerRecordMetadata has the message's topic, partition, offset, timestamp, timestamp type, key, and headers. It can be used to store the key and timestamp in an event log,  downstream can fork by partition into parallel streams, timestamps and their type can be updated to show a process state change, offsets can be tracked, and headers can add custom metadata.

Once all polled records pass completely through the stream, ConsumerRecordsToQueue resumes pulling causing KafkaSource to commit the  messages and then it polls again.

```scala
val dispatcher = ActorAttributes.dispatcher("dendrites.blocking-dispatcher")
def shoppingCartCmdEvtSource(dispatcher: Attributes)(implicit ec: ExecutionContext, logger: LoggingAdapter):
Source[(String, ShoppingCartCmd), NotUsed] = {

val kafkaSource = KafkaSource[String, Array[Byte]](ShoppingCartCmdConsumer).withAttributes(dispatcher)
val consumerRecordQueue = new ConsumerRecordsToQueue[String, Array[Byte]](extractRecords)
val deserializer = new ConsumerRecordDeserializer[String, ShoppingCartCmd](toCaseClass)
kafkaSource.via(consumerRecordQueue).via(deserializer)
}

val runnableGraph = shoppingCartCmdEvtSource(dispatcher)
.via(userFlows) // custom flows here
.to(sink)
```

#### Kafka as a stream sink

<img src="png/KafkaSinkStream.png?raw=true" width="50%" />

###### Common stages of streams ending with a KafkaSink, upstream can be anything

Serialize case classes, send them, and handle errors.

AvroSerializer takes a user’s Avro schema and a serialize function: [ccToByteArray](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/avro/package.scala){:target="_blank"} can serialize case classes with simple field types. Those with complex fields need a user defined serialize function. [Avro4sSerializer](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/avro4s/stream/Avro4sSerializer.scala){:target="_blank"} is also provided and is often easier to use.

KafkaSink wraps the driver and configures a [KafkaProducer](http://kafka.apache.org/0110/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html) and calls its asynchronous send method. It takes care of Java & Scala futures, callbacks and exceptions. Send returns a [Guava ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained){:target="_blank"} with a Kafka [Callback](http://kafka.apache.org/0110/javadoc/org/apache/kafka/clients/producer/Callback.html){:target="_blank"}. Success invokes an Akka [AsyncCallback](http://doc.akka.io/docs/akka/current/scala/stream/stream-customize.html#using-asynchronous-side-channels){:target="_blank"} that pulls from upstream. Failure invokes an AsyncCallback which invokes a Supervision Decider. Stop exceptions stop the stream. Kafka’s [RetriableException](http://kafka.apache.org/0110/javadoc/org/apache/kafka/connect/errors/RetriableException.html){:target="_blank"}s are retried. If `enable.idempotence=true` the driver handles retriable exceptions instead of throwing them it also eliminates duplicate sends, if it's `false` KafkaSink handles them with [exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff){:target="_blank"} delays. 

[ProducerConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/ProducerConfig.scala) is a trait for configuring KafkaProducer and creating a [ProducerRecord](http://kafka.apache.org/0110/javadoc/org/apache/kafka/clients/producer/ProducerRecord.html), it has 6 constructors. You extend ProducerConfig with your choice of constructor. Default is topic, key, value leaving Kafka to select the partition and create the timestamp.

```scala
val ap = AccountProducer // example custom configured KafkaProducer
val serializer = new AvroSerializer("getAccountBalances.avsc", ccToByteArray)
val sink = KafkaSink[String, Array[Byte]](ap)
val runnableGraph = source.via(serializer).to(sink)
```


#### Split Kafka streams by Topic Partition
Kafka [TopicPartitions](http://kafka.apache.org/0100/javadoc/org/apache/kafka/common/TopicPartition.html){:target="_blank"} can be processed in parallel, ConsumerRecords can be split into 2 to 22 parallel streams, one per topic partition.

![image](png/2PartitionKafkaSourceStream.png?raw=true)
###### Streams beginning with a KafkaSource that forks parallel downstreams by Kafka partition
dualConsumerRecordsFlow takes a ConsumerRecords for 2 TopicPartitions and enqueues them in 2 queues and pushes them as a [Tuple2](http://www.scala-lang.org/api/current/scala/Tuple2.html){:target="_blank"}.

[Unzip](http://doc.akka.io/docs/akka/current/scala/stream/stages-overview.html#unzip){:target="_blank"} is a built in Akka Stream stage that splits the tuple into 2 outputs.

The rest of the stages are the same but duplicated in parallel.

tripleConsumerRecordsFlow is provided to extract 3 Topic Partitions.

#### Example Configurations

[Typesafe Config](https://github.com/typesafehub/config){:target="_blank"} example, and optional, config settings for Kafka are in `src/main/resources/reference.conf`. You can choose to use Typesafe Config and override these in your application's `src/main/resources/application.conf`.

Example consumer configuration in `src/main/resources/kafkaConsumer.properties`

Example producer configuration in `src/main/resources/kafkaProducer.properties`

#### Other Kafka Streaming products

[Kafka Streams](http://kafka.apache.org/documentation/streams/){:target="_blank"} has a new Transactions API said to guarantee exactly once processing,  *dendrites* is at least once but you can eliminate duplicates on the Producer side setting `enable.idempotence=true` and compensate for duplicates, or filter them, on the consumer side.

[Reactive Kafka](https://github.com/akka/reactive-kafka){:target="_blank"} also has a Source and Sink for Akka Streams.
