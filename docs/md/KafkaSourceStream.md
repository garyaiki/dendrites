A Kafka Source Stream polls Kafka, enqueues records, dequeues single record for each pull, extracts message value, deserializes to case class. Commits poll only if all records transit the stream.

![image](https://github.com/garyaiki/dendrites/blob/master/docs/png/KafkaSourceStream.png?raw=true)
[KafkaSource](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/kafka/stream/KafkaSource.scala) constructs a [KafkaConsumer](http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html) which polls Kafka. It pushes a [ConsumerRecords](http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/ConsumerRecords.html) containing all records returned. If poll or commit throws a retriable exception they are retried with [exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff) delays. 

[consumerRecordsFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/kafka/stream/package.scala) enqueues the records.

[ConsumerRecordQueue](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/kafka/stream/ConsumerRecordQueue.scala) pushes one record downstream each time it is pulled. It only pulls from upstream when the queue is empty.

[consumerRecordValueFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/kafka/stream/package.scala) extracts the value from each [ConsumerRecord](http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/ConsumerRecord.html)

[AvroDeserializer](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/avro/stream/AvroDeserializer.scala) is constructed with a user’s Avro schema and a user function that maps an Avro [GenericRecord](http://avro.apache.org/docs/current/api/java/org/apache/avro/generic/GenericRecord.html) to a case class ([example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/examples/account/avro/package.scala)).

When all records have passed completely through the stream, ConsumerRecordQueue pulls from upstream causing KafkaSource to commit the offsets of messages polled and then it polls again.

An exception thrown in the stream (other than a retriable in KafkaSource) should stop the stream. This is default behavior. Stopping the stream means KafkaSource doesn’t commit messages polled so the messages will be polled again.