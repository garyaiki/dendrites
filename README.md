# dendrites

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Scala library of streaming components for building Microservices, Event Sourcing, Event Logging, CQRS, and Reactive systems. Functions and pre-built stream stages for Akka Streams, HTTP, Actors, Kafka, Cassandra, Algebird, and Avro.

This Readme covers download, setup and configuration, unit and integration testing and adding it to your project.
Documentation is on the [dendrites website](https://garyaiki.github.io/dendrites/), Scaladocs have examples and lower level descriptions.

#### Add dendrites to your project
##### Download from Maven Central
[dendrites](http://mvnrepository.com/artifact/com.github.garyaiki)
##### sbt
Add dependency in `build.sbt`

`libraryDependencies += "com.github.garyaiki" % "dendrites_2.12" % "0.6.0"`
##### Maven
Add dependency in `pom.xml`

    `<!-- https://mvnrepository.com/artifact/com.github.garyaiki/dendrites_2.12 -->
    <dependency>
        <groupId>com.github.garyaiki</groupId>
        <artifactId>dendrites_2.12</artifactId>
        <version>0.6.0</version>
    </dependency>`

#### Build from source
Requires Java 8 
[download jdk8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and Scala 2.12 [download scala](http://www.scala-lang.org/download/). Clone or download dendrites from Github, see 
[cloning-a-repository](https://help.github.com/articles/cloning-a-repository/)

#### Build and test with sbt
[sbt version](https://github.com/garyaiki/dendrites/blob/master/project/build.properties),  [download](http://www.scala-sbt.org/download.html),  [documentation](http://www.scala-sbt.org/documentation.html)

##### sbt commands
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

`> doc` Generates Scaladocs in `/dendrites/target/scala-2.12/api`

`> scalastyle` Run scalastyle on source code under `src/main/scala`

`> test:scalastyle` Run scalastyle on source code under `src/test/scala`

`> dependencyTree` Prints an ascii tree of all the dependencies to the console

`> exit` quit sbt
#### Build and test with Maven
[Maven](https://maven.apache.org/index.html)

##### Maven commands
In a terminal window `cd` to the dendrites directory.

`$ mvn dependency:resolve` Resolves and optionally retrieves dependencies.

`$ mvn clean` Deletes files produced by the build under `target` directory

`$ mvn compile` Compiles sources under `src/main/scala`

`$ mvn scala:testCompile` Compiles unit test sources under `src/test/scala`

`$ mvn test` Executes unit tests.

`$ mvn install` Install the package in local repository, for use as in other projects locally.

`$ mvn source:jar` Bundle the main sources of the project into a jar archive.

`$ mvn scala:doc-jar` Generate Scaladocs in a jar

#### Kafka
Minimal install and configure for running dendrites integration tests. [documentation](http://kafka.apache.org/documentation) [download](http://kafka.apache.org/downloads)

Extract server files `tar -xvf kafka_2.1*-0.1*.*.*.tar`

Optionally, create or replace symbolic link `ln -nsf kafka_2.1*-0.1*.*.* kafka`

##### Configure server
Edit configuration in install directory `/config/server.properties`

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
Add dependencies to driver jars. sbt and Maven dependency settings. See [kafka-clients Maven repository page](http://mvnrepository.com/artifact/org.apache.kafka/kafka-clients)

##### Configure driver's Kafka Consumer
Put a Consumer Config properties file on your classpath. See Kafka's [New Consumer Configs](http://kafka.apache.org/documentation#newconsumerconfigs) documentation and a dendrites [example](https://github.com/garyaiki/dendrites/blob/master/src/main/resources/kafkaConsumer.properties). KafkaSource requires `enable.auto.commit=false` for stream backpressure to control commit. To pass this configuration to KafkaSource, create a subclass of [ConsumerConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/ConsumerConfig.scala). [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/kafka/AccountConsumer.scala)
##### Configure driver's Kafka Producer
Put a Producer Config properties file on your classpath. See Kafka's [Producer Configs](http://kafka.apache.org/documentation#producerconfigs) documentation  [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/kafka/AccountProducer.scala) To pass this configuration to KafkaSink, create a subclass of [ProducerConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/kafka/ProducerConfig.scala) [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/kafka/AccountProducer.scala)
##### Run Kafka server
In a Terminal window `cd` to kafka install directory
###### Start Zookeeper
`bin/zookeeper-server-start.sh config/zookeeper.properties`

Zookeeper will run in the foreground
###### Start Kafka server
In a 2nd Terminal window in the same directory

`bin/kafka-server-start.sh config/server.properties`

Kafka server will run in the foreground
###### Create topic for 1 partition

In a 3rd Terminal window in the same directory

`bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic account-topic`

This takes a few seconds.

###### Run integration tests
Kafka server must be running and have an `account-topic`

In dendrites directory terminal window with sbt running

`> it:compile`

`> it:testOnly com.github.garyaiki.dendrites.kafka.KafkaProducerConsumerSpec`

`> it:testOnly com.github.garyaiki.dendrites.kafka.AvroKafkaProducerConsumerSpec`

`> it:testOnly com.github.garyaiki.dendrites.kafka.stream.KafkaStreamSpec`

###### After tests, delete topic
List topics

`bin/kafka-topics.sh --zookeeper localhost:2181 --list`

Delete topic, this may not happen right away

`bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic account-topic`

###### ShoppingCartCmd integration tests
###### Create a topic for 1 partition
`bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic shoppingcartcmd-topic`

`it:testOnly com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.ShoppingCartCmdSpec`
###### After tests, delete topic

`bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic shoppingcartcmd-topic`

###### After all tests, stop Kafka server, stop Zookeeper
After topic deleted, stop Kafka server

`bin/kafka-server-stop.sh`

Give Zookeeper a few seconds. Then stop it too

`bin/zookeeper-server-stop.sh`

#### Cassandra
Minimal install and configure for running dendrites integration tests, [documentation](http://www.planetcassandra.org/apache-cassandra-documentation/) [download](http://www.planetcassandra.org/cassandra/)

Extract files `tar -xvf apache-cassandra-3.9-bin.tar`

Optionally, create or replace symbolic link `ln -nsf apache-cassandra-3.9 cassandra`
##### Configure
For development, default configuration can be used.
##### Install Cassandra Java Driver
Add dependencies to `cassandra-driver-core`, `cassandra-driver-mapping`, `cassandra-driver-extras`. Driver jars with sbt and Maven dependency settings. See [Datastax Cassandra Maven repository page](http://mvnrepository.com/artifact/com.datastax.cassandra)

##### Configure Cassandra Client
dendrites has an [example Cassandra configuration](https://github.com/garyaiki/dendrites/blob/master/src/main/resources/reference.conf) in `resource.conf` under `dendrites/cassandra`. You can override these in your `application.conf` file. [CassandraConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/CassandraConfig.scala) is a trait you can extend. [PlayListSongConfig](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/cassandra/PlaylistSongConfig.scala) is an example.
##### Run Cassandra
In a Terminal window `cd` to cassandra install directory

`bin/cassandra -f`

It will run in the foreground
###### Run integration tests

In a dendrites directory terminal window with sbt running. These tests teardown their keyspaces, tables, and connections, run them one at a time so ScalaTest doesn't mix them up and report false errors.

`> it:compile`

`> it:testOnly com.github.garyaiki.dendrites.cassandra.BoundStmtSpec`

`> it:testOnly com.github.garyaiki.dendrites.cassandra.stream.CassandraSongSpec`

`> it:testOnly com.github.garyaiki.dendrites.cassandra.stream.CassandraPlaylistSpec`

`> it:testOnly com.github.garyaiki.dendrites.cassandra.stream.CassandraSpec`

`> it:testOnly com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.stream.CassandraShoppingCartSpec`

`> it:testOnly com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.stream.CassandraShoppingCartCmdSpec`

`> it:testOnly com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.stream.CassandraShoppingCartEvtLogSpec`

`> it:testOnly com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.stream.ShoppingCartCmdAndEvtSinkSpec`

`> it:testOnly com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.stream.ShoppingCartCmdAndEvtSinksSpec`

With Kafka shoppingcartcmd-topic

`> it:testOnly com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.stream.ShoppingCartCmdAndEvtSpec`


###### Stop Cassandra
Exit sbt to close its connection to Cassandra. If you kill Cassandra while sbt is still  it will keep trying to reconnect to Cassandra.

Then, `Ctrl-C` in the terminal running Cassandra

#### Akka Http
[documentation](http://doc.akka.io/docs/akka-http/current/scala.html)

`Balances Server` is an example server that handles requests from integration tests under `src/it/scala`
##### Run Balances Server
These integration tests need Balances Server running to handle HTTP requests. Open a 2nd dendrites directory terminal window and start sbt in this window to run the server.

`> run` Runs `Balances Server` an Http server
###### Run integration tests

In the dendrites directory terminal with sbt running

`> it:compile`

`> it:testOnly com.github.garyaiki.dendrites.http.stream.TypedQueryResponseFlowSpec`

`> it:testOnly com.github.garyaiki.dendrites.examples.account.http.*`

Sometimes, some of these tests fail because Balances Server has a default limit of 4 concurrent requests and tests are running in parallel and may not get time on the server. If this happens, re-run the failed tests one at a time.
###### Stop Balances Server
`Ctrl-C` in the terminal window running Balances Server

### Typesafe Config
dendrites uses Typesafe Config. Traits with names ending in `Config` define configuration properties for components. This mediates configuration from usage, so you may use these traits with different configuration sources.

[Typesafe Config user guide](https://github.com/typesafehub/config),
[Akka config user guide](http://doc.akka.io/docs/akka/current/scala/general/configuration.html)

Configurations in `/src/main/resources/reference.conf` can be overridden by your `application.conf`.

The `akka` section is for logging and has Akka specific logging settings

Under the `dendrites` section

`algebird` has default settings for CountMinSketch, DecayedValue, HyperLogLog, and QTree

`blocking-dispatcher` configures a thread pool to be used when there are blocking calls

`checking-balances`, `money-market-balances`, and `savings-balances` are example http client configurations

`kafka` is an example topic and timeout configuration

`cassandra` is an example cluster and keyspace configuration

`timer` is asychronous and exponential backoff timeout configuration

### Logback configuration

[Logback configuration guide](http://logback.qos.ch/manual/configuration.html)

Logging configuration for running dendrites tests is in `/src/main/resources/logback.xml`

Your application Logback configuration is done separately.

[Logging Separation](http://logback.qos.ch/manual/loggingSeparation.html)


