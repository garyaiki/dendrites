
### Kafka, Avro streaming components

{% include nav.html %}
Build distributed streaming systems with pre-built Kafka and Avro stream stages.

[<img src="png/KafkaSink.png?raw=true" alt="KafkaSink" width="25%" height="25%" title="input serialized value, publish it, uses Akka Supervision to retry temporary errors with exponential backoff or fail the stream">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/KafkaSink.scala){:target="_blank"}
[<img src="png/KafkaSource.png?raw=true" alt="KafkaSource" width="23%" height="23%" title="input Kafka, output ConsumerRecords">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/KafkaSource.scala){:target="_blank"}
[<img src="png/consumerRecordsDequeue.png?raw=true" alt="consumerRecordsDequeue" width="21%" height="21%" title="input ConsumerRecords, output ConsumerRecord one at a time">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/ConsumerRecordsToQueue.scala){:target="_blank"}
[<img src="png/dualConsumerRecordsFlow.png?raw=true" alt="dualConsumerRecordsFlow" width="25%" height="25%" title="input ConsumerRecords from 2 partitions, output tuple of Queue of ConsumerRecord with queue for each partition">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/package.scala){:target="_blank"}
[<img src="png/tripleConsumerRecordsFlow.png?raw=true" alt="tripleConsumerRecordsFlow" width="25%" height="25%" title="input ConsumerRecords from 3 partitions, output tuple of Queue of ConsumerRecord with queue for each partition">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/package.scala){:target="_blank"}
[<img src="png/extractValueFlow.png?raw=true" alt="extractValueFlow" width="20%" height="20%" title="input ConsumerRecord, output its value">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/package.scala){:target="_blank"}
[<img src="png/ConsumerRecordDeserializer.png?raw=true" alt="ConsumerRecordDeserializer" width="25%" height="25%" title="input a ConsumerRecord, output a tuple of Kafka key and case class value">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/avro4s/ConsumerRecordDeserializer.scala){:target="_blank"}
[<img src="png/AvroDeserializer.png?raw=true" alt="AvroDeserializer" width="20%" height="20%" title="input an Avro serialized array of bytes, output the case class it maps to">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/avro4s/stream/Avro4sDeserializer.scala){:target="_blank"}
[<img src="png/AvroSerializer.png?raw=true" alt="AvroSerializer" width="20%" height="20%" title="input a case class, output an Avro serialized array of bytes">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/avro4s/stream/Avro4sSerializer.scala){:target="_blank"}
###### Click image to open source code in a new tab. Hover over image for stage inputs and outputs

[Kafka](https://engineering.linkedin.com/kafka/benchmarking-apache-kafka-2-million-writes-second-three-cheap-machines){:target="_blank"} has quickly become the preferred distributed system hub for [microservices](https://martinfowler.com/articles/microservices.html){:target="_blank"}, [CRQS](https://martinfowler.com/bliki/CQRS.html){:target="_blank"}, [Event Sourcing](https://www.confluent.io/blog/event-sourcing-cqrs-stream-processing-apache-kafka-whats-connection/){:target="_blank"} and traditional messaging. Scala's rich language features combined with Akka Streams allows most Kafka coding to be generalized, including [backpressure](http://www.reactivemanifesto.org/glossary#Back-Pressure){:target="_blank"} to prevent overflow, local error handling with recovery, and reliable message processing.


##### Kafka as a stream source

<p >
<img src="png/KafkaSourceStream.png?raw=true" width="50%" />
</p>
[KafkaSource](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/KafkaSource.scala){:target="_blank"} wraps Kafka's built-in Java driver and constructs a [KafkaConsumer](http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html){:target="_blank"} which polls messages asynchronously. Available messages are returned in a [ConsumerRecords](http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/ConsumerRecords.html){:target="_blank"} container. If either polling or committing throws a retriable exception it's retried with [exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff){:target="_blank"} delays. 

[ConsumerRecordsToQueue](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/ConsumerRecordsToQueue.scala){:target="_blank"} maps messages to a Queue of ConsumerRecord. It dequeues on each downstream pull. It only pulls from upstream when the queue is empty.

[ConsumerRecordDeserializer](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/avro4s/ConsumerRecordDeserializer.scala){:target="_blank"} maps ConsumerRecord values to your case class. Two flavors of ConsumerRecordDeserializer are provided, one uses [Avro4s](https://github.com/sksamuel/avro4s){:target="_blank"} ([example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/avro4s/Avro4sBalance.scala){:target="_blank"}), the other uses Avro schema and [GenericRecord](http://avro.apache.org/docs/current/api/java/org/apache/avro/generic/GenericRecord.html){:target="_blank"} serialized to `Array[Byte]` ([example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/avro/package.scala){:target="_blank"}).

When all records have passed completely through the stream, ConsumerRecordsToQueue pulls from upstream causing KafkaSource to commit the offsets of messages polled and then it polls again returning new records.

KafkaSource uses [Supervision](http://doc.akka.io/docs/akka/current/scala/stream/stream-error.html){:target="_blank"}, an important part of Akka Actors and Akka Streams.
Exceptions thrown in the stream (other than a [RetriableException](http://kafka.apache.org/0100/javadoc/org/apache/kafka/common/errors/RetriableException.html){:target="_blank"} in KafkaSource) should stop the stream. This is default behavior. Stopping the stream means KafkaSource doesn’t commit offsets so records in a failed stream will be polled again. Exceptions mapped to Restart and Resume in any stage of a stream **will** cause KafkaSource to commit.

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

##### Split Kafka streams by Topic Partition
Kafka [TopicPartitions](http://kafka.apache.org/0100/javadoc/org/apache/kafka/common/TopicPartition.html){:target="_blank"} can be processed in parallel, ConsumerRecords can be split into 2 to 22 parallel streams, one per topic partition.

![image](png/2PartitionKafkaSourceStream.png?raw=true)
[dualConsumerRecordsFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/package.scala){:target="_blank"} takes a ConsumerRecords for 2 TopicPartitions and enqueues them in 2 queues and pushes them as a [Tuple2](http://www.scala-lang.org/api/current/scala/Tuple2.html){:target="_blank"}.

[Unzip](http://doc.akka.io/docs/akka/2.4/scala/stream/stages-overview.html#unzip){:target="_blank"} is a built in Akka Stream stage that splits the tuple into 2 outputs.

The rest of the stages are the same but duplicated in parallel.

[tripleConsumerRecordsFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/package.scala){:target="_blank"} is provided to extract 3 Topic Partitions.

##### Kafka as a stream sink
A Kafka [Sink](http://doc.akka.io/docs/akka/current/scala/stream/stream-flows-and-basics.html#Defining_and_running_streams){:target="_blank"} stream serializes case classes, asynchronously calls Kafka's send method and handles errors.

![image](png/KafkaSinkStream.png?raw=true)

[AvroSerializer](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/avro/stream/AvroSerializer.scala){:target="_blank"} takes a user’s Avro schema and a serialize function. [ccToByteArray](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/avro/package.scala){:target="_blank"} can serialize case classes that just have simple field types to a byte array. Case classes with complex fields need a user defined serialize function. There is also an [Avro4s](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/avro4s/stream/Avro4sSerializer.scala){:target="_blank"} serializer stage.

[KafkaSink](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/stream/KafkaSink.scala){:target="_blank"} needs no user defined code other than [KafkaProducer](http://kafka.apache.org/0100/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html){:target="_blank"} settings in a custom subclass of [ProducerConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/ProducerConfig.scala){:target="_blank"}. The sink asynchronously writes to Kafka. The Java driver immediately returns a [Guava ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained){:target="_blank"} with a Kafka [Callback](http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/producer/Callback.html){:target="_blank"}. Success invokes an Akka [AsyncCallback](http://doc.akka.io/docs/akka/current/scala/stream/stream-customize.html#using-asynchronous-side-channels){:target="_blank"} that pulls from upstream. Failure invokes an AsyncCallback that invokes a Supervision [Decider](http://doc.akka.io/docs/akka/current/scala/stream/stream-error.html){:target="_blank"}. Stop exceptions stop the stream. Kafka’s [RetriableException](http://kafka.apache.org/0100/javadoc/org/apache/kafka/connect/errors/RetriableException.html){:target="_blank"}s are retried with [exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff){:target="_blank"} delays.

```scala
val ap = AccountProducer // example custom configured KafkaProducer
val serializer = new AvroSerializer("getAccountBalances.avsc", ccToByteArray)
val sink = KafkaSink[String, Array[Byte]](ap)
val runnableGraph = source.via(serializer).to(sink)
```


