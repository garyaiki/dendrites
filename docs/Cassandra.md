### Cassandra streaming components

{% include nav.html %}
Build distributed database clients with pre-built Cassandra stream stages.

[<img src="png/CassandraBind.png?raw=true" alt="CassandraBind" width="20%" height="20%" title="input a case class, output a BoundStatement">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraBind.scala){:target="_blank"}
[<img src="png/CassandraBoundQuery.png?raw=true" alt="CassandraBoundQuery" width="25%" height="25%" title="input a case class, output a ResultSet">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraBoundQuery.scala){:target="_blank"}
[<img src="png/CassandraConditional.png?raw=true" alt="CassandraConditional" width="25%" height="25%" title="input a BoundStatement for a Conditional operation, output an optional Row">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraConditional.scala){:target="_blank"}
[<img src="png/CassandraMappedPaging.png?raw=true" alt="CassandraMappedPaging" width="25%" height="25%" title="input a ResultSet, output a sequence of case classes it maps to">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraMappedPaging.scala){:target="_blank"}
[<img src="png/CassandraPaging.png?raw=true" alt="CassandraPaging" width="20%" height="20%" title="input a ResultSet, output a sequence of Row">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraPaging.scala){:target="_blank"}
[<img src="png/CassandraQuery.png?raw=true" alt="CassandraQuery" width="25%" height="25%" title="input a BoundStatement, output a ResultSet">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraQuery.scala){:target="_blank"}
[<img src="png/CassandraRetrySink.png?raw=true" alt="CassandraRetrySink" width="20%" height="20%" title="input a case class execute an operation on it, retry temporary errors with exponential backoff">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraRetrySink.scala){:target="_blank"}
[<img src="png/CassandraSink.png?raw=true" alt="CassandraSink" width="20%" height="20%" title="input a BoundStatement, execute it">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraSink.scala){:target="_blank"}
###### Click image to open source code in a new tab. Hover over image for stage inputs and outputs

[Cassandra](https://academy.datastax.com/planet-cassandra/what-is-apache-cassandra){:target="_blank"} is one of the most popular distributed databases. *dendrites* generalizes frequently used Cassandra operations. Stream stages use backpressure to prevent overflow and in-stage error handling with recovery. Pre-built Cassandra stream stages wrap Datastax’s Cassandra [Java Driver](http://docs.datastax.com/en/developer/java-driver//3.1/){:target="_blank"} and call it asynchronously.

#### Setup client and connect

Before running streams, a cluster connection is configured with a [CassandraConfig](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/CassandraConfig.scala){:target="_blank"}, initialized and managed with [provided functions](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/package.scala){:target="_blank"}. [PreparedStatements](http://docs.datastax.com/en/developer/java-driver//3.1/manual/statements/prepared/){:target="_blank"} are pre-parsed in the database.
```scala
createClusterSchemaSession(ShoppingCartConfig, 1)
CassandraShoppingCart.createTable(session, schema)
CassandraShoppingCartEvtLog.createTable(session, schema)
prepStmts = prepareStatements(session, schema)
```

#### Query from a stream
<img src="png/CassandraQueryStream.png?raw=true" width="60%" />

[CassandraBind](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraBind.scala){:target="_blank"} binds values to a PreparedStatement with a user defined function (UDF) to create a [BoundStatement](http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/BoundStatement.html){:target="_blank"}. Example insert, query bind [UDF](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/cassandra/Playlists.scala){:target="_blank"}

[CassandraQuery](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraQuery.scala){:target="_blank"} executes BoundStatements. The Java driver returns a [ResultSetFuture](http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/ResultSetFuture.html){:target="_blank"} (which extends Guava's [ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained){:target="_blank"}). This is mapped to a Scala Future. It's Success invokes an Akka [AsyncCallback](http://doc.akka.io/docs/akka/current/scala/stream/stream-customize.html#Using_asynchronous_side-channels){:target="_blank"} which pushes the [ResultSet](http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/ResultSet.html){:target="_blank"}.

[CassandraBoundQuery](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraBoundQuery.scala){:target="_blank"} binds and queries in a single stage.

[CassandraPaging](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraPaging.scala){:target="_blank"} pulls a ResultSet, having any number of [Rows](http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/Row.html){:target="_blank"}, and pushes a specified number of them.

[CassandraMappedPaging](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraMappedPaging.scala){:target="_blank"} pushes a specified number of user case classes instead of Rows.

An Akka Stream [built-in map stage](http://doc.akka.io/docs/akka/current/scala/stream/stages-overview.html#map){:target="_blank"} is created with a UDF that maps Rows to case classes. [ScalaCass](https://github.com/thurstonsand/scala-cass){:target="_blank"} helps do the mapping ([example](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/com/github/garyaiki/dendrites/cassandra/Playlists.scala){:target="_blank"})

```scala
val bndStmt = new CassandraBind(Playlists.playlistsPrepQuery(session, schema), playlistToBndQuery)
val query = new CassandraQuery(session)
val paging = new CassandraPaging(10)
def toPlaylists: Flow[Seq[Row], Seq[Playlist], NotUsed] =
  Flow[Seq[Row]].map(Playlists.rowsToPlaylists)

val runnableGraph = source
  .via(bndStmt)
  .via(query).via(paging)
  .via(toPlaylists)
  .to(sink)
```

#### Insert, delete from a stream
[CassandraSink](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraSink.scala){:target="_blank"} executes delete and insert BoundStatements. It wraps the Java driver and calls it asynchronously.

```scala
val bndStmt = new CassandraBind(Playlists.playlistsPrepInsert(session, schema), playlistToBndInsert)
val sink = new CassandraSink(session)
val runnableGraph = source.via(bndStmt).to(sink)
```

#### Update from a stream

Updating any database risks updating dirty data (it's updated since your last read). With [eventually consistent databases](https://en.wikipedia.org/wiki/Eventual_consistency){:target="_blank"} there's added risk. [CassandraRetrySink](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraRetrySink.scala){:target="_blank"} uses [Lightweight Transactions](http://docs.datastax.com/en/cassandra/2.1/cassandra/dml/dml_ltwt_transaction_c.html){:target="_blank"} with [conditional statements](http://docs.datastax.com/en/dse/5.1/cql/cql/cql_using/useScanPartition.html){:target="_blank"} and [optimistic locking](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.OptimisticLocking.html){:target="_blank"}. Your data must have a version number and your statement has a query to check it. If current, update is executed. If not, it uses the current version and retries (with exponential backoff). CassandraRetrySink accepts an A => ResultSetFuture function, where A is usually a case class. It can execute different statements depending on the content of A ([example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/cqrs/shoppingcart/cmd/package.scala){:target="_blank"}). This is slower than insert: try to design for insert over update.

#### Cassandra conditional flows 
[CassandraConditional](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraConditional.scala){:target="_blank"} is for conditional updates. They return a ResultSetFuture with just one Row, its first column is "applied" and it's true on success. If it failed the Row is wrapped in an Option and pushed to the next stage to handle the result.

[CassandraKeyValueFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/stream/CassandraKeyValueFlow.scala){:target="_blank"} also executes conditional statements. On Success, the input key/case class value is pushed. On fail after exhausting retries the stage fails stopping the stream. Useful for event logging where this stage is a service and a downstream stage logs the event. 

#### Cassandra stand alone functions
There are several [functions](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/cassandra/package.scala){:target="_blank"} for Cassandra cluster, schema, session, and logging that can be used with or without Akka Streams.


