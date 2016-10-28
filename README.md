# dendrites

A Scala library for building Reactive systems with Akka Streams, HTTP, Actors, and Agents, Kafka, Cassandra, Twitter's Algebird, Spray, and Avro. It blends Scala's functional programming with Akka's resilient non-blocking message passing concurrency and Akka Stream's debuggable, pluggable, testable, and Reactive component design. 

Introductory documentation is on the [Wiki](https://github.com/garyaiki/dendrites/wiki), Scaladocs go into detail.
#### Build from source
Requires Java 8 
[download sdk](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and
Scala 2.11 
[downlod scala](http://www.scala-lang.org/download/)

Clone or download dendrites from Github, see 
[cloning-a-repository](https://help.github.com/articles/cloning-a-repository/)

##### Build and test with sbt
dendrites sbt [version](https://github.com/garyaiki/dendrites/blob/master/project/build.properties), download [sbt](http://www.scala-sbt.org/download.html), sbt's [documentation](http://www.scala-sbt.org/documentation.html)


###### sbt commands
In a terminal window `cd` to the dendrites directory.

`$ sbt` launch sbt, returns `>` prompt

`> exit` quit sbt

`> help` list available help

`> clean` Deletes files produced by the build under `target` directory

`> compile` Compiles sources under `src/main/scala`

`> test:compile` Compiles unit test sources under `src/test/scala`

`> it:compile` Compiles integration test sources under `src/it/scala`

`> test` Executes unit tests.

`> testOnly` Executes specific unit tests

`> testQuick` Re-executes failed unit tests

`> package` Produces the jar.

`> doc` Generates Scaladocs in `/dendrites/target/scala-2.11/api`

`> scalastyle` Run scalastyle on source code under `src/main/scala`

`> update` Resolves and optionally retrieves dependencies.

`> dependencyTree` Prints an ascii tree of all the dependencies to the console
##### Build and test with Maven
Maven [home page](https://maven.apache.org/index.html)

In a dendrites directory terminal window

`$ mvn clean`

`$ mvn compile`

`$ mvn scala:testCompile`

`$ mvn test`

`$ mvn install`
#### Download jar from Maven Central


[dendtites](http://mvnrepository.com/artifact/com.github.garyaiki) jar will soon be on Maven Central

Add dependency in sbt `build.sbt`

`libraryDependencies += "com.github.garyaiki" % "dendrites_2.11" % "0.4.0"`

Add dependency in Maven `pom.xml`

    `<!-- https://mvnrepository.com/artifact/com.github.garyaiki/dendrites_2.11 -->
    <dependency>
        <groupId>com.github.garyaiki</groupId>
        <artifactId>dendrites_2.11</artifactId>
        <version>0.4.0</version>
    </dependency>
`

#### Kafka
Kafka's [documentation](http://kafka.apache.org/documentation)
##### Install server
[Download](http://kafka.apache.org/downloads) Kafka for Scala version 2.11

Extract files

`tar -xvf kafka_2.11-0.10.1.0.tar`

Optionally, create or replace symbolic link

`ln -nsf kafka_2.11-0.10.1.0 kafka`

##### Configure server
Edit configuration in Kafka install directory `/config/server.properties`

###### Optional settings for development
You may want to delete topics. In `Server Basics` un-comment

`delete.topic.enable=true`

To advertise the broker in a local install. In `Socket Server Settings`
add

`listeners=PLAINTEXT://localhost:9092`

`port=9092`

`host.name=localhost`

To shorten log retention hours. In `Log Retention Policy`

`log.retention.hours=1`
##### Install Kafka Java Driver
Add dependencies to driver jars. sbt and Maven dependency settings are under
kafka-clients [repo page](http://mvnrepository.com/artifact/org.apache.kafka/kafka-clients)
##### Configure driver's Kafka Consumer
Put a Consumer Config properties file on your classpath. See Kafka's [New Consumer Configs](http://kafka.apache.org/documentation#newconsumerconfigs) documentation and a dendrites [example](https://github.com/garyaiki/dendrites/blob/master/src/main/resources/kafkaConsumer.properties). KafkaSource requires `enable.auto.commit=false` for stream backpressure to control commit. To pass this configuration to KafkaSource, create a subclass of [ConsumerConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/ConsumerConfig.scala). See this [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/kafka/AccountConsumer.scala)
##### Configure driver's Kafka Producer
Put a Producer Config properties file on your classpath. See Kafka's [Producer Configs](http://kafka.apache.org/documentation#producerconfigs) documentation and a dendrites [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/kafka/AccountProducer.scala) To pass this configuration to KafkaSink, create a subclass of [ProducerConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/ProducerConfig.scala) See this [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/kafka/AccountProducer.scala)
##### Run Kafka server
In a Terminal window `cd` to kafka install directory
###### Start Zookeeper
`bin/zookeeper-server-start.sh config/zookeeper.properties`

Zookeeper will run in the foreground
###### Start Kafka server
In a 2nd Terminal window in the same directory

`bin/kafka-server-start.sh config/server.properties`

Kafka server will run in the foreground
###### Create a topic for 1 partition
change `account-topic` to your topic name.

In a 3rd Terminal window in the same directory

`bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic account-topic`

Shell prompt after topic creation

##### Run integration tests
Kafka server must be running and have `account-topic`

In dendrites directory terminal window with sbt running

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



#### Cassandra
Cassandra [documentation](http://www.planetcassandra.org/apache-cassandra-documentation/)
##### Install server
Dowload [Cassandra](http://www.planetcassandra.org/cassandra/)

Extract files

`tar -xvf apache-cassandra-3.9-bin.tar`

Optionally, create or replace symbolic link

`ln -nsf apache-cassandra-3.9 cassandra`
##### Configure Cassandra
For development, default configuration can be used.
##### Install Cassandra Java Driver
Add dependencies to `cassandra-driver-core`, `cassandra-driver-mapping`, `cassandra-driver-extras`. Driver jars with sbt and Maven dependency settings are under Datastax Cassandra [repo page](http://mvnrepository.com/artifact/com.datastax.cassandra) 
##### Configure Cassandra Client
dendrites example Cassandra configuration setup can be used, but doesn't have to be. [Example values](https://github.com/garyaiki/dendrites/blob/master/src/main/resources/reference.conf) are in `resource.conf` under `dendrites/cassandra`. You can override these in your `application.conf`. [CassandraConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/CassandraConfig.scala) is a trait you can use in an object or class. [PlayListSongConfig](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/cassandra/PlaylistSongConfig.scala) is an example.
##### Run Cassandra
In a Terminal window `cd` to cassandra install directory

`bin/cassandra -f`

Cassandra will run in the foreground
##### Run integration tests
Cassandra must be running

In a dendrites directory terminal window with sbt running

`> it:compile`

`> it:testOnly com.github.garyaiki.dendrites.cassandra.BoundStmtSpec`

`> it:testOnly com.github.garyaiki.dendrites.cassandra.stream.CassandraSongSpec`

`> it:testOnly com.github.garyaiki.dendrites.cassandra.stream.CassandraPlaylistSpec`

`> it:testOnly com.github.garyaiki.dendrites.cassandra.stream.CassandraSpec`
##### Stop Cassandra
After running integration tests, exit sbt to close its connection to Cassandra. If you kill Cassandra while sbt is still running after the tests it will keep trying to reconnect to Cassandra.

Then kill it in the terminal window running Cassandra

Ctrl-C
#### Akka Http
Akka Http [documentation](http://doc.akka.io/docs/akka-http/current/scala.html)

`Balances Server` is an example server that handles requests from integration tests under `src/it/scala`
##### Run Balances Server
In a dendrites directory terminal window with sbt running

`> run` Runs `Balances Server` an Http server
##### Run integration tests
Balances Server must be running

In a dendrites directory terminal window with sbt running

`> it:compile`

`> it:testOnly com.github.garyaiki.dendrites.http.stream.TypedQueryResponseFlowSpec`

`> it:testOnly com.github.garyaiki.dendrites.examples.account.http.*`

Sometimes, some of these tests fail because Balances Server has the default limit of 4 concurrent requests and tests are running in parallel and may not get time on the server. If this happens, re-run the failed tests one at a time.
##### Stop Balances Server
Kill it in the terminal window running Balances Server

Ctrl-C
### License
dendrites is Open Source and available under the Apache 2 License.

