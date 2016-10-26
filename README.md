# dendrites

A library to concurently receive and aggregate messages and services for Scala and Akka that is asynchronous, non-blocking, and controls back pressure to prevent data loss from overflow.

Restful clients and servers using
[Akka HTTP](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0/scala/http/index.html) with Scala case class ↔︎ Json
marshalling/unmarshalling using [spray-json](https://github.com/spray/spray-json). Results from parallel calls are aggregated asynchronously using reuseable [Akka Streams](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0/scala/stream-index.html) components. 

Messages to and from [Akka Actors](http://doc.akka.io/docs/akka/current/scala.html) can be aggregated with a reusable version of the [Akka Aggregator Pattern](http://doc.akka.io/docs/akka/snapshot/contrib/aggregator.html).

Results from a mix of actors and Rest services can be aggregated together.

Statistical approximations of these results are derived using [Twitter Algebird](https://github.com/twitter/algebird) for [Averaged Value](http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm), [Count-min sketch](https://en.wikipedia.org/wiki/Count–min_sketch),
[Decayed Value](https://github.com/twitter/algebird/wiki/Using-DecayedValue-as-moving-average), [HyperLogLog](https://en.wikipedia.org/wiki/HyperLogLog), and [QTree](https://github.com/twitter/algebird/wiki/QTree)
are accumulated in [Akka Agents](http://doc.akka.io/docs/akka/snapshot/scala/agents.html) which can be read and updated by these clients safely and concurrently.

### Kafka
[Documentation](http://kafka.apache.org/documentation)
##### Install server
[Download](http://kafka.apache.org/downloads)  server for Scala version 2.11
Extract
`tar -xvf kafka_2.11-0.10.1.0.tar`
Optionally, create or replace symbolic link
`ln -nsf kafka_2.11-0.10.1.0 kafka`

##### Configure server
Edit install directory/config/server.properties

###### For development and optional
You may want to delete topics. In `Server Basics` un-comment `delete.topic.enable=true`
To advertise the broker in a local install. In `Socket Server Settings`
add these settings
`listeners=PLAINTEXT://localhost:9092`

`port=9092`

`host.name=localhost`
To shorten log retention. In `Log Retention Policy` shorten hours
`log.retention.hours=1`

##### Run Kafka server
In a Terminal window `cd` to kafka directory
###### Start Zookeeper
`bin/zookeeper-server-start.sh config/zookeeper.properties`
Zookeeper will run in the foreground
###### Start Kafka server
In a 2nd Terminal window in the kafka directory

`bin/kafka-server-start.sh config/server.properties`
Kafka will run in the foreground
###### Create a topic for 1 partition
change `account-topic` to your topic name.

In a 3rd Terminal window in the kafka directory

`bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic account-topic`
Shell prompt after creation
##### Delete topic, stop Kafka server, stop Zookeeper
First, delete topic

`bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic account-topic`

Give Kafka and Zookeeper a few seconds. Then Stop Kafka

`bin/kafka-server-stop.sh`

Give Zookeeper a few seconds. Then Stop it too

`bin/zookeeper-server-stop.sh`

##### Install Kafka Java Driver
[kafka-clients](http://mvnrepository.com/artifact/org.apache.kafka/kafka-clients) version 0.10.0.1
##### Configure Kafka client Consumer
Put a Consumer Config properties file on your classpath. See [New Consumer Configs](http://kafka.apache.org/documentation#newconsumerconfigs) documentation and an [example](https://github.com/garyaiki/dendrites/blob/master/src/main/resources/kafkaConsumer.properties) KafkaSource requires `enable.auto.commit=false` for stream backpressure to control commit. To pass this configuration to KafkaSource, create a subclass of [ConsumerConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/ConsumerConfig.scala) See this [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/kafka/AccountConsumer.scala)
##### Configure Kafka client Producer
Put a Producer Config properties file on your classpath. See [Producer Configs](http://kafka.apache.org/documentation#producerconfigs) documentation and an [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/kafka/AccountProducer.scala) To pass this configuration to KafkaSink, create a subclass of [ProducerConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/ProducerConfig.scala) See this [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/kafka/AccountProducer.scala)

### Cassandra
[Documentation](http://www.planetcassandra.org/apache-cassandra-documentation/)
##### Install server
[Download](http://www.planetcassandra.org/cassandra/) latest version
Extract
`tar -xvf apache-cassandra-3.9-bin.tar`
Optionally, create or replace symbolic link
`ln -nsf apache-cassandra-3.9 cassandra`
##### Configure server
For development, default configuration can be used.
##### Run Cassandra
In a Terminal window `cd` to cassandra directory
`bin/cassandra -f`

Cassandra will run in the foreground
#### Stop Cassandra
Cntrl-C
##### Install Cassandra Java Driver
[Maven Central](http://mvnrepository.com/artifact/com.datastax.cassandra) download latest version of cassandra-driver-core, cassandra-driver-mapping, cassandra-driver-extras
##### Configure Cassandra Client
Unlike with KafkaSource and KafkaSing, Cassandra configuration can be wherever. See [example values](https://github.com/garyaiki/dendrites/blob/master/src/main/resources/reference.conf) under `dendrites/cassandra` You can override these in your `application.conf` or define your own. [CassandraConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/CassandraConfig.scala) is a trait you can use in an object or class. [PlayListSongConfig](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/cassandra/PlaylistSongConfig.scala) is an example.