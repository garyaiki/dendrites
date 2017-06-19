### Kafka, Avro streaming components

{% include nav.html %}
Build distributed streaming systems with pre-built Kafka and Avro stream stages.

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

[Kafka](https://engineering.linkedin.com/kafka/benchmarking-apache-kafka-2-million-writes-second-three-cheap-machines){:target="_blank"} is the preferred  hub for [microservices](https://martinfowler.com/articles/microservices.html){:target="_blank"}, [CRQS](https://martinfowler.com/bliki/CQRS.html){:target="_blank"}, [Event Sourcing](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/){:target="_blank"} and  messaging systems. Scala's rich language features combined with Akka Streams allow a lot of Kafka coding to be generalized, including [backpressure](http://www.reactivemanifesto.org/glossary#Back-Pressure){:target="_blank"} to prevent overflow, in-stage error handling with recovery, and reliable message processing.


#### Kafka as a stream source

<p >
<img src="png/KafkaSourceStream.png?raw=true" width="50%" />
</p>
###### Common stages of streams beginning with a KafkaSource, downstream can be anything

Streams with a KafkaSource poll messages from a Kafka cluster, deserialize them, and commit messages once processed.

Kafka is written in Scala but its built in driver is written in Java.

KafkaSource wraps the driver and configures a [KafkaConsumer](http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html){:target="_blank"} which polls and commits messages.

Polling and commit are rare blocking calls in *dendrites*, so KafkaSource uses a blocking ExecutionContext (Akka Stream’s [default ExecutionContext](http://doc.akka.io/docs/akka/2.5.1/scala/dispatchers.html#default-dispatcher){:target="_blank"} has about as many threads as cpu cores, blocking could cause thread starvation).

Available messages are returned in a [ConsumerRecords](http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/ConsumerRecords.html){:target="_blank"} object. If either polling or committing throws Kafka's [RetriableExceptions](http://kafka.apache.org/0102/javadoc/index.html?org/apache/kafka/common/errors/RetriableException.html){:target="_blank"} it's retried with [exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff){:target="_blank"} delays.

Kafka tracks messages with offsets. Committing a message means its offset is marked as committed. Kafka auto-commits by default but this only assures they were read OK. By turning off auto-commit we can do it after processing, assuring they were read *and processed* OK. If any failed, Kafka re-polls those messages.

KafkaSource uses [Supervision](http://doc.akka.io/docs/akka/current/scala/stream/stream-error.html){:target="_blank"}, a key benefit of Akka Actors and Streams. When KafkaSource is in a stream, exceptions thrown anywhere in the stream (other than  retriable exceptions) should stop the stream. Stopping the stream means KafkaSource doesn’t commit offsets so records in a failed stream will be polled again.

ConsumerRecordsToQueue initially queues every ConsumerRecord contained in ConsumerRecords. Then it pushes one ConsumerRecord for each downstream pull. If the queue is not empty it doesn't pull from upstream. This is to ensure all messages have been processed. After the queue empties it resumes pulling, causing KafkaSource to commit its previous poll and poll anew.

ConsumerRecordDeserializer maps ConsumerRecord values to case classes. Two flavors of ConsumerRecordDeserializer are provided, one takes a UDF ([example with Avro4s](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/avro4s/Avro4sGetAccountBalances.scala){:target="_blank"}) to deserialize from an Array of Bytes to a case class, the other takes an Avro schema and maps to a [GenericRecord](http://avro.apache.org/docs/current/api/java/org/apache/avro/generic/GenericRecord.html){:target="_blank"}, which is in turn mapped by a UDF to a case class
serialized to `Array[Byte]` ([example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/avro/package.scala){:target="_blank"}). Either way a deserialized case class and the ConsumerRecord's key is pushed.

Once all polled records pass completely through the stream, ConsumerRecordsToQueue resumes pulling causing KafkaSource to commit the  messages and then it polls again.

```scala
/** Kafka poll and commit block, adding a dispatcher avoids possible thread starvation
ShoppingCartCmdConsumer is a user specific subclass of KafkaConsumer to configure the
driver
*/
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
<p >
<img src="png/KafkaSinkStream.png?raw=true" width="50%" />
</p>
###### Common stages of streams ending with a KafkaSink, upstream can be anything

A KafkaSink stream serializes case classes, asynchronously calls Kafka's send method and handles errors.

AvroSerializer takes a user’s Avro schema and a serialize function. [ccToByteArray](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/avro/package.scala){:target="_blank"} can serialize case classes that only have simple field types. Those with complex fields need a user defined serialize function. [Avro4sSerializer](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/avro4s/stream/Avro4sSerializer.scala){:target="_blank"} is also provided and is often easier to use.

KafkaSink wraps the driver and configures a [KafkaProducer](http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html) and calls its asynchronous send method. It takes care of Java & Scala futures, callbacks and retries Kafka's [RetriableExceptions](http://kafka.apache.org/0102/javadoc/index.html?org/apache/kafka/common/errors/RetriableException.html){:target="_blank"}. Send immediately returns a [Guava ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained){:target="_blank"} with a Kafka [Callback](http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/producer/Callback.html){:target="_blank"}. Success invokes an Akka [AsyncCallback](http://doc.akka.io/docs/akka/current/scala/stream/stream-customize.html#using-asynchronous-side-channels){:target="_blank"} that pulls from upstream. Failure invokes an AsyncCallback which invokes a Supervision [Decider](http://doc.akka.io/docs/akka/current/scala/stream/stream-error.html){:target="_blank"}. Stop exceptions stop the stream. Kafka’s [RetriableException](http://kafka.apache.org/0100/javadoc/org/apache/kafka/connect/errors/RetriableException.html){:target="_blank"}s are retried with [exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff){:target="_blank"} delays.

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

[Unzip](http://doc.akka.io/docs/akka/2.4/scala/stream/stages-overview.html#unzip){:target="_blank"} is a built in Akka Stream stage that splits the tuple into 2 outputs.

The rest of the stages are the same but duplicated in parallel.

tripleConsumerRecordsFlow is provided to extract 3 Topic Partitions.
