### Cassandra streaming components

{% include nav.html %}
Build distributed database clients with stream stages and stand-alone functions.

[<img src="png/CassandraQuery.png?raw=true" alt="CassandraQuery" width="25%" height="25%" title="input a BoundStatement, output a ResultSet">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraQuery.scala){:target="_blank"}
[<img src="png/CassandraPaging.png?raw=true" alt="CassandraPaging" width="20%" height="20%" title="input a ResultSet, output a sequence of Row">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraPaging.scala){:target="_blank"}
[<img src="png/CassandraSink.png?raw=true" alt="CassandraSink" width="20%" height="20%" title="input a BoundStatement, execute it">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraSink.scala){:target="_blank"}
[<img src="png/CassandraRetrySink.png?raw=true" alt="CassandraRetrySink" width="20%" height="20%" title="input a case class execute an operation on it, retry temporary errors with exponential backoff">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraRetrySink.scala){:target="_blank"}
[<img src="png/CassandraBoundQuery.png?raw=true" alt="CassandraBoundQuery" width="25%" height="25%" title="input a case class, output a ResultSet">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraBoundQuery.scala){:target="_blank"}
[<img src="png/CassandraMappedPaging.png?raw=true" alt="CassandraMappedPaging" width="25%" height="25%" title="input a ResultSet, output a sequence of case classes it maps to">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraMappedPaging.scala){:target="_blank"}
[<img src="png/CassandraConditional.png?raw=true" alt="CassandraConditional" width="25%" height="25%" title="input a BoundStatement for a Conditional operation, output an optional Row">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraConditional.scala){:target="_blank"}

###### Click image to open source code in a new tab. Hover over image for stage inputs and outputs

[Cassandra](https://academy.datastax.com/planet-cassandra/what-is-apache-cassandra){:target="_blank"} query, sink, and conditional stages wrap Datastax’s [Java Driver](http://docs.datastax.com/en/developer/java-driver//3.1/){:target="_blank"} and call it asynchronously. They handle errors with recovery in-stage. Paging stages use backpressure to push a page at a time. Pre and and post stream functions initialize and clean up client operations. 

#### Stand-alone functions

[Functions](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/package.scala){:target="_blank"} for clusters, schemas, sessions, and logging.

```scala
val config = CassandraConfig
val addresses = config.getInetAddresses
...
val cluster = createCluster(addresses, retryPolicy, reConnectPolicy)
val lbp = createLoadBalancingPolicy(config.localDataCenter)
initLoadBalancingPolicy(cluster, lbp)
logMetadata(cluster)
registerQueryLogger(cluster)
val session = connect(cluster)
val schema = config.keySpace
val strategy = config.replicationStrategy
createSchema(session, schema, strategy, repCount)
...
val resultSet = executeBoundStmt(session, bndStmt)
...
val colNames: StringBuilder = getRowColumnNames(row)
val sessionInfo: String = sessionLogInfo(session)
...
dropSchema(session, schema)
close(session, cluster)
```

#### Setup client and connect

Before running streams, a cluster connection is configured, initialized and managed with provided functions. [CassandraConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/CassandraConfig.scala){:target="_blank"} is an optional trait you can extend. [PreparedStatements](http://docs.datastax.com/en/developer/java-driver//3.1/manual/statements/prepared/){:target="_blank"} are pre-parsed in the database.
```scala
createClusterSchemaSession(ShoppingCartConfig, 1)
CassandraShoppingCart.createTable(session, schema)
CassandraShoppingCartEvtLog.createTable(session, schema)
prepStmts = prepareStatements(session, schema)
```
###### createClusterSchemaSession is an example of grouping stand-alone functions with user defined settings

#### Query from a stream
<img src="png/CassandraQueryStream.png?raw=true" width="60%" />

CassandraBind is Akka Streams' [built-in map stage](http://doc.akka.io/docs/akka/current/scala/stream/stages-overview.html#map){:target="_blank"} used to create a [BoundStatement](http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/BoundStatement.html){:target="_blank"} from a PreparedStatement, a case class, and a user defined function (UDF). Example insert, query bind [UDFs](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/cassandra/Playlists.scala){:target="_blank"}.

[CassandraQuery](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraQuery.scala){:target="_blank"} executes BoundStatements. The driver returns a [ResultSetFuture](http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/ResultSetFuture.html){:target="_blank"} (which extends Guava's [ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained){:target="_blank"}). It's mapped to a Scala Future. Success invokes an Akka [AsyncCallback](http://doc.akka.io/docs/akka/current/scala/stream/stream-customize.html#Using_asynchronous_side-channels){:target="_blank"} which pushes the [ResultSet](http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/ResultSet.html){:target="_blank"}.

[CassandraPaging](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraPaging.scala){:target="_blank"} pulls a ResultSet, having any number of [Rows](http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/Row.html){:target="_blank"}, and pushes a specified number of them.

A map stage is created with a UDF that maps Rows to case classes. [ScalaCass](https://github.com/thurstonsand/scala-cass){:target="_blank"} helps do the mapping ([example](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/cassandra/Playlists.scala){:target="_blank"})

```scala
val partialBndQuery = bndQuery(Playlists.prepQuery(session, schema), _: UUID)
val query = new CassandraQuery(session)
val paging = new CassandraPaging(10)

val runnableGraph = source
  .map(partialBndQuery)
  .via(query).via(paging)
  .map(Playlists.mapRows)
  .to(sink)
```
###### Before running, DB parses PreparedStatement, it's partially applied to bind function, later, UUID passed as stage input.

[CassandraBoundQuery](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraBoundQuery.scala){:target="_blank"} binds and queries in a single stage. There's an additional check that upstream completion doesn't cause this stage to complete before pushing its result. 


[CassandraMappedPaging](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraMappedPaging.scala){:target="_blank"} pushes a specified number of case classes after mapping them from Rows.

```scala
val query = new CassandraBoundQuery[UUID](session, prepStmt, bndQuery, 1)
val paging = new CassandraMappedPaging[ShoppingCart](10, mapRows)
source.via(query).via(paging).runWith(sink)
```

#### Insert, delete from a stream
[CassandraSink](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraSink.scala){:target="_blank"} is for executing insert and delete.

```scala
val partialBndInsert = bndInsert(Playlists.prepInsert(session, schema), _: Playlist)
val sink = new CassandraSink(session)
source.map(partialBndInsert).runWith(sink)
```

#### Update from a stream

Updating any database risks updating dirty data (it changed since your last read). With [eventually consistent databases](https://en.wikipedia.org/wiki/Eventual_consistency){:target="_blank"} there's more risk. [CassandraRetrySink](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraRetrySink.scala){:target="_blank"} uses [Lightweight Transactions](http://docs.datastax.com/en/cassandra/2.1/cassandra/dml/dml_ltwt_transaction_c.html){:target="_blank"} with [conditional statements](http://docs.datastax.com/en/dse/5.1/cql/cql/cql_using/useScanPartition.html){:target="_blank"} and [optimistic locking](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.OptimisticLocking.html){:target="_blank"}. Your data must have a version number and your statement has a query to check it. If current, update is executed. If not, it uses the current version and retries (with exponential backoff). CassandraRetrySink accepts an A => ResultSetFuture function, where A is usually a case class. It can execute different statements depending on the content of A ([example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/cmd/package.scala){:target="_blank"}). This is slower than insert: try to design for insert over update.

```scala
val curriedCheckAndSetOwner = checkAndSetOwner(session, prepQueryStmt, prepStmt) _
val source = Source[SetOwner](iter)
val sink = new CassandraRetrySink[SetOwner](RetryConfig, curriedCheckAndSetOwner).withAttributes(dispatcher)
source.runWith(sink)
```
###### Curry function to query then update depending on query result before running, later, pass case class and call function.

#### Cassandra conditional flows 
[CassandraConditional](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraConditional.scala){:target="_blank"} is a Flow for conditional updates. They return a ResultSetFuture with just one Row, its first column is "applied" and is true on success. If it failed the Row is wrapped in an Option and pushed to the next stage. Use this if you want to handle the result.

```scala
val partialBndUpdateItems = bndUpdateItems(prepStmt, _: SetItems)
val curriedErrorHandler = getConditionalError(rowToString) _
val conditional = new CassandraConditional(session, curriedErrorHandler)
val runnableGraph = source.map(partialBndUpdateItems).via(conditional)...
```
###### Partially apply function with 1 argument list, curry function with 2 argument lists.

[CassandraKeyValueFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraKeyValueFlow.scala){:target="_blank"} also executes conditional statements. On Success, the input key/case class value is pushed. On fail, after exhausting retries, the stage fails stopping the stream. Useful for  downstream event logging. With a KafkaSource, ConsumerRecordMetadata can be used as the Key passing on Kafka metadata.
```scala
val curriedDoCmd = doShoppingCartCmd(session, prepStmts) _
val cmdFlowGraph = new CassandraKeyValueFlow[String, ShoppingCartCmd](RetryConfig, curriedDoCmd).withAttributes(dispatcher)
val cmdFlow = Flow.fromGraph(cmdFlowGraph)
val optInsEvtPrepStmt = prepStmts.get("InsertEvt")
val insEvtPrepStmt = optInsEvtPrepStmt match {
  case Some(x) => x
  case None => throw new NullPointerException("ShoppingCartEvt Insert preparedStatement not found")
}
val partialBndInsert = bndInsert(insEvtPrepStmt, _: ShoppingCartEvt)
val sink = new CassandraSink(session)
cmdFlow.map(cmdToEvt).map(partialBndInsert).to(sink)
```
###### Execute command, map input metadata & command case class to event case class, log event.

#### Example Configurations

[Typesafe Config](https://github.com/typesafehub/config){:target="_blank"} example, and optional, config settings for Cassandra are in `src/main/resources/reference.conf`. You can choose to use Typesafe Config and override these in your application's `src/main/resources/application.conf` See [Akka config user guide](http://doc.akka.io/docs/akka/current/scala/general/configuration.html){:target="_blank"}.

#### Other Cassandra Streaming products

Lightbend's Alpakka module has a [CassandraConnector](http://developer.lightbend.com/docs/alpakka/current/cassandra.html){:target="_blank"}, with a Source and Sink.


