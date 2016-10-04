Multiple Kafka [TopicPartitions](http://kafka.apache.org/0100/javadoc/org/apache/kafka/common/TopicPartition.html) can be polled, records for each partition can be processed in parallel.
![image](https://github.com/garyaiki/dendrites/blob/master/docs/png/2PartitionKafkaSourceStream.png?raw=true)
[dualConsumerRecordsFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/kafka/stream/package.scala) takes a ConsumerRecords for 2 TopicPartitions and enqueues them in 2 queues and pushes them in a [Tuple2](http://www.scala-lang.org/api/current/index.html#scala.Tuple2).

[Unzip](http://doc.akka.io/docs/akka/2.4/scala/stream/stages-overview.html#unzip) is a built in Akka Stream stage that splits the tuple into 2 outputs.

The rest is the same as Kafka Source Stream.

[tripleConsumerRecordsFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/kafka/stream/package.scala) is provided to extract 3 Topic Partitions.