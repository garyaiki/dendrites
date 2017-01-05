# dendrites

A Scala library for building Reactive systems with Akka Streams, HTTP, Actors, and Agents, Kafka, Cassandra, Twitter's Algebird, Spray, and Avro. It blends Scala's functional programming with Akka's resilient non-blocking message passing concurrency and Akka Stream's debuggable, pluggable, testable, and Reactive component design. 

Introductory documentation is on the [Wiki](https://github.com/garyaiki/dendrites/wiki), Scaladocs go into detail.
#### Build from source
Requires Java 8 
[download sdk](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and
Scala 2.11 
[download scala](http://www.scala-lang.org/download/)

Clone or download dendrites from Github, see 
[cloning-a-repository](https://help.github.com/articles/cloning-a-repository/)

##### Build and test with sbt
[version](https://github.com/garyaiki/dendrites/blob/master/project/build.properties),  [download](http://www.scala-sbt.org/download.html),  [documentation](http://www.scala-sbt.org/documentation.html)

###### commands
In a terminal window `cd` to the dendrites directory.

`$ sbt` launch sbt, returns `>` prompt

`> help` list available help

`> update` Resolves and optionally retrieves dependencies.

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

`> dependencyTree` Prints an ascii tree of all the dependencies to the console

`> exit` quit sbt
##### Build and test with Maven
Maven [home page](https://maven.apache.org/index.html)

###### commands
In a terminal window `cd` to the dendrites directory.

`$ mvn dependency:resolve` Resolves and optionally retrieves dependencies.

`$ mvn clean` Deletes files produced by the build under `target` directory

`$ mvn compile` Compiles sources under `src/main/scala`

`$ mvn scala:testCompile` Compiles unit test sources under `src/test/scala`

`$ mvn test` Executes unit tests.

`$ mvn install` Install the package in local repository, for use as in other projects locally.

`$ mvn source:jar` Bundle the main sources of the project into a jar archive.

`$ mvn scala:doc-jar` Generate Scaladocs in a jar
#### Add dendrites to your project
##### Download dendrites jar from Maven Central

[dendrites](http://mvnrepository.com/artifact/com.github.garyaiki)
###### sbt
Add dependency in sbt `build.sbt`

`libraryDependencies += "com.github.garyaiki" % "dendrites_2.11" % "0.4.1"`
###### Maven
Add dependency in Maven `pom.xml`

    `<!-- https://mvnrepository.com/artifact/com.github.garyaiki/dendrites_2.11 -->
    <dependency>
        <groupId>com.github.garyaiki</groupId>
        <artifactId>dendrites_2.11</artifactId>
        <version>0.4.1</version>
    </dependency>
`

#### Kafka local installation, configuration, and dendrites integration tests
Kafka's [documentation](http://kafka.apache.org/documentation)
##### Install server
[Download](http://kafka.apache.org/downloads) Kafka for Scala version 2.11

Extract files

`tar -xvf kafka_2.11-0.10.1.1.tar`

Optionally, create or replace symbolic link

`ln -nsf kafka_2.11-0.10.1.1 kafka`

##### Configure server
Edit configuration in Kafka install directory `/config/server.properties`

###### Optional settings for development
For development, you will want to delete topics. In `Server Basics` un-comment

`delete.topic.enable=true`

To advertise the broker in a local install. In `Socket Server Settings` add

`listeners=PLAINTEXT://localhost:9092`

`port=9092`

`host.name=localhost`

In production, log retention hours are 168 (1 week). For development you can shorten them to 1. In `Log Retention Policy`

`log.retention.hours=1`
##### Install Kafka Java Driver
Add dependencies to driver jars. sbt and Maven dependency settings are under
kafka-clients [Maven repository page](http://mvnrepository.com/artifact/org.apache.kafka/kafka-clients)
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

It completes in a few seconds.

##### Run integration tests
Kafka server must be running and have an `account-topic`

In dendrites directory terminal window with sbt running

`> it:compile`

`> it:testOnly com.github.garyaiki.dendrites.kafka.KafkaProducerConsumerSpec`

`> it:testOnly com.github.garyaiki.dendrites.kafka.AvroKafkaProducerConsumerSpec`

`> it:testOnly com.github.garyaiki.dendrites.kafka.stream.KafkaStreamSpec`

##### After testing, delete topic, stop Kafka server, stop Zookeeper
First, delete topic

`bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic account-topic`

Give Kafka and Zookeeper a few seconds. Then stop Kafka

`bin/kafka-server-stop.sh`

Give Zookeeper a few seconds. Then stop it too

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
Add dependencies to `cassandra-driver-core`, `cassandra-driver-mapping`, `cassandra-driver-extras`. Driver jars with sbt and Maven dependency settings are under Datastax Cassandra [Maven repository page](http://mvnrepository.com/artifact/com.datastax.cassandra) 
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

Then, in the terminal window running Cassandra

Ctrl-C
#### Akka Http
Akka Http [documentation](http://doc.akka.io/docs/akka-http/current/scala.html)

`Balances Server` is an example server that handles requests from integration tests under `src/it/scala`
##### Run Balances Server
The HTTP integration tests need Balances Server running to handle HTTP requests. You can open a 2nd dendrites directory terminal window and start sbt in this window just to run the server.

`> run` Runs `Balances Server` an Http server
##### Run integration tests
Balances Server must be running

In the dendrites directory terminal window used to compile and run tests with sbt running

`> it:compile`

`> it:testOnly com.github.garyaiki.dendrites.http.stream.TypedQueryResponseFlowSpec`

`> it:testOnly com.github.garyaiki.dendrites.examples.account.http.*`

Sometimes, some of these tests fail because Balances Server has a default limit of 4 concurrent requests and tests are running in parallel and may not get time on the server. If this happens, re-run the failed tests one at a time.
##### Stop Balances Server
In the terminal window running Balances Server

Ctrl-C

### Typesafe Config
dendrites uses Typesafe Config. Traits with names ending in `Config` define configuration properties for components. This mediates configuration from usage, so you may use these traits with different configuration sources.

[Typesafe Config user guide](https://github.com/typesafehub/config),
[Akka config user guide](http://doc.akka.io/docs/akka/2.4/general/configuration.html)

Each configuration in `/src/main/resources/reference.conf` can be overridden by your `application.conf`.

The `akka` section is for logging and has Akka specific logging settings

Under the `dendrites` section

`algebird` has default settings for CountMinSketch, DecayedValue, HyperLogLog, and QTree

`blocking-dispatcher` configures a thread pool to be used when there are blocking calls

These other sections are examples, that don't need to be overriden.
`checking-balances`, `money-market-balances`, and `savings-balances` are example http client configurations

`kafka` is an example topic configuration

`cassandra` is an example configuration

### Logback configuration

[Logback configuration guide](http://logback.qos.ch/manual/configuration.html)

Logging configuration for running dendrites tests is in `/src/main/resources/logback.xml`

Application Logback configuration is done separately.

[Logging Separation](http://logback.qos.ch/manual/loggingSeparation.html)

### License
dendrites is Open Source and available under the Apache 2 License.

