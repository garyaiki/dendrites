A Kafka [Sink](http://doc.akka.io/docs/akka/2.4/scala/stream/stream-flows-and-basics.html#Defining_and_running_streams) Stream serializes case class messages, asynchronously sends to Kafka, handles errors.
![image](https://github.com/garyaiki/dendrites/blob/master/docs/png/KafkaSinkStream.png?raw=true)

[AvroSerializer](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/avro/stream/AvroSerializer.scala) takes a user’s Avro schema and a serialize function and serializes case class messages. [ccToByteArray](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/avro/package.scala) can serialize a case class with simple field types to a byte array. Case classes with complex fields need a user defined function.

[KafkaSink](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/kafka/stream/KafkaSink.scala) is a generic flow that is constructed with a pre configured [KafkaProducer](http://kafka.apache.org/0100/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html). It asynchronously sends the serialized message to Kafka. The Java driver immediately returns a [Guava ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained) with a Kafka [Callback](kafka.apache.org/0100/javadoc/org/apache/kafka/clients/producer/Callback.html). Success invokes an [AsyncCallback](http://doc.akka.io/docs/akka/2.4/scala/stream/stream-customize.html#Using_asynchronous_side-channels) that invokes a pull from upstream. Failure invokes an AsyncCallback that invokes a Supervision [Decider](http://doc.akka.io/docs/akka/2.4.11/scala/stream/stream-error.html). Stop exceptions stop the stream. Kafka’s [RetriableException](http://kafka.apache.org/0100/javadoc/org/apache/kafka/connect/errors/RetriableException.html)s are retried with [exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff) delays.

```scala
val ap = AccountProducer // configured KafkaProducer
val serializer = new AvroSerializer("getAccountBalances.avsc", ccToByteArray)
val sink = KafkaSink[String, Array[Byte]](ap)
val runnableGraph = source.via(serializer).to(sink)
```