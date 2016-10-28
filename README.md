# dendrites

A Scala library for building Reactive systems with Akka Streams, HTTP, Actors, and Agents, Kafka, Cassandra, Twitter's Algebird, Spray, and Avro. It offers functions, streaming sources, flows, and sinks, and examples of runnable streams that blend generic with custom code.

See the [Wiki](https://github.com/garyaiki/dendrites/wiki) for an introduction and overview.
### Build from source
##### Java 8 sdk required
[download sdk](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
##### Scala 2.11 required
[downlod scala](http://www.scala-lang.org/download/)
##### Clone dendrites source from Github
[cloning-a-repository](https://help.github.com/articles/cloning-a-repository/)

In a terminal window

`$ git help clone`
##### Build and test with sbt
[download sbt](http://www.scala-sbt.org/download.html)

[sbt's Documentation](http://www.scala-sbt.org/documentation.html)

[sbt version](https://github.com/garyaiki/dendrites/blob/master/project/build.properties)
###### sbt commands
In a terminal window `cd` to the directory.

`$ sbt` launch sbt, returns `>` prompt

`> exit` quit sbt

`> help` list available help

`> help clean` Deletes files produced by the build.

`> help compile` Compiles sources.

`> help test` Executes unit tests.

`> help testOnly` Executes the tests provided as arguments

`> help testQuick` Executes tests that failed before

`> help package` Produces the jar.

`> help run` Runs `Balances Server` an Http server

`> help doc` Generates API documentation under `/dendrites/target/scala-2.11/api`

`> help scalastyle` Run scalastyle on source code

`> help update` Resolves and optionally retrieves dependencies, producing a report.

`> help dependencyTree` Prints an ascii tree of all the dependencies to the console
##### Build and test with Maven
In a terminal window

`$ mvn clean`

`$ mvn compile`

`$ mvn install`
### Download jar
[Maven home page](https://maven.apache.org/index.html)

[dendtites](http://mvnrepository.com/artifact/com.github.garyaiki) jar will soon be on Maven Central

    `<!-- https://mvnrepository.com/artifact/com.github.garyaiki/dendrites_2.11 -->
    <dependency>
        <groupId>com.github.garyaiki</groupId>
        <artifactId>dendrites_2.11</artifactId>
        <version>0.4.0</version>
    </dependency>
`



### Kafka
[Kafka's Documentation](http://kafka.apache.org/documentation)
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
##### Install Kafka Java Driver
[kafka-clients](http://mvnrepository.com/artifact/org.apache.kafka/kafka-clients) version 0.10.0.1
##### Configure Kafka client Consumer
Put a Consumer Config properties file on your classpath. See [New Consumer Configs](http://kafka.apache.org/documentation#newconsumerconfigs) documentation and an [example](https://github.com/garyaiki/dendrites/blob/master/src/main/resources/kafkaConsumer.properties) KafkaSource requires `enable.auto.commit=false` for stream backpressure to control commit. To pass this configuration to KafkaSource, create a subclass of [ConsumerConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/ConsumerConfig.scala) See this [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/kafka/AccountConsumer.scala)
##### Configure Kafka client Producer
Put a Producer Config properties file on your classpath. See [Producer Configs](http://kafka.apache.org/documentation#producerconfigs) documentation and an [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/kafka/AccountProducer.scala) To pass this configuration to KafkaSink, create a subclass of [ProducerConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/ProducerConfig.scala) See this [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/kafka/AccountProducer.scala)
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

##### Run integration tests
Kafka server must be running and have `account-topic`

In terminal window with sbt running

`> it:compile`

`> it:testOnly com.github.garyaiki.dendrites.kafka.KafkaProducerConsumerSpec`

`> it:testOnly com.github.garyaiki.dendrites.kafka.AvroKafkaProducerConsumerSpec`

`> it:testOnly com.github.garyaiki.dendrites.kafka.stream.KafkaStreamSpec`

##### Delete topic, stop Kafka server, stop Zookeeper
First, delete topic

`bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic account-topic`

Give Kafka and Zookeeper a few seconds. Then Stop Kafka

`bin/kafka-server-stop.sh`

Give Zookeeper a few seconds. Then Stop it too

`bin/zookeeper-server-stop.sh`



### Cassandra
[Cassandra Documentation](http://www.planetcassandra.org/apache-cassandra-documentation/)
##### Install server
[Download](http://www.planetcassandra.org/cassandra/) latest version
Extract
`tar -xvf apache-cassandra-3.9-bin.tar`
Optionally, create or replace symbolic link
`ln -nsf apache-cassandra-3.9 cassandra`
##### Configure server
For development, default configuration can be used.
##### Install Cassandra Java Driver
[Driver jars on Maven Central](http://mvnrepository.com/artifact/com.datastax.cassandra) download latest version of cassandra-driver-core, cassandra-driver-mapping, cassandra-driver-extras
##### Configure Cassandra Client
Unlike with KafkaSource and KafkaSing, Cassandra configuration can be wherever. See [example values](https://github.com/garyaiki/dendrites/blob/master/src/main/resources/reference.conf) under `dendrites/cassandra` You can override these in your `application.conf` or define your own. [CassandraConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/CassandraConfig.scala) is a trait you can use in an object or class. [PlayListSongConfig](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/cassandra/PlaylistSongConfig.scala) is an example.
##### Run Cassandra
In a Terminal window `cd` to cassandra directory
`bin/cassandra -f`

Cassandra will run in the foreground
##### Run integration tests
Cassandra must be running

In terminal window with sbt running

`> it:compile`

`> it:testOnly com.github.garyaiki.dendrites.cassandra.BoundStmtSpec`

`> it:testOnly com.github.garyaiki.dendrites.cassandra.stream.CassandraSongSpec`

`> it:testOnly com.github.garyaiki.dendrites.cassandra.stream.CassandraPlaylistSpec`

`> it:testOnly com.github.garyaiki.dendrites.cassandra.stream.CassandraSpec`
#### Stop Cassandra
After running integration tests, exit sbt to close its connection to Cassandra

Then kill it

Cntrl-C
