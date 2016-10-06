Querying [Cassandra](http://cassandra.apache.org) can use mostly generic flows and Datastax’s [Java Driver](http://docs.datastax.com/en/developer/java-driver//3.1/)
![image](https://github.com/garyaiki/dendrites/blob/master/docs/png/CassandraQueryStream.png?raw=true)

[PreparedStatements](http://docs.datastax.com/en/developer/java-driver//3.1/manual/statements/prepared/) can be parsed by Cassandra before the stream runs. [CassandraBind](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/cassandra/stream/CassandraBind.scala) flow binds message values to the PreparedStatement with a user defined function. The [BoundStatement](http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/BoundStatement.html) is pushed to the next stage.

[CassandraQuery](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/cassandra/stream/CassandraQuery.scala) is a generic flow that asynchronously executes the BoundStatement. The Java driver immediately returns a [ResultSet Future](http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/ResultSetFuture.html) which extends Guava's [ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained). It is converted to a Scala Future and uses its onComplete. Success invokes an Akka Stream [AsyncCallback](http://doc.akka.io/docs/akka/2.4/scala/stream/stream-customize.html#Using_asynchronous_side-channels) which pushes the ResultSet. Failure invokes an AsyncCallback which fails the stage.

[CassandraPaging](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/cassandra/stream/CassandraPaging.scala) is a generic flow that pushes a user specified page size Sequence of [Row](http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/Row.html) from the ResultSet.

A built in Akka Stream map flow takes a user defined function to map Rows to case classes. [ScalaCass](https://github.com/thurstonsand/scala-cass) does the mapping ([example](https://github.com/garyaiki/dendrites/blob/master/src/it/scala/org/gs/cassandra/Playlists.scala))

```scala
val bndStmt = new CassandraBind(Playlists.playlistsPrepQuery(session, schema),
                    playlistToBndQuery)
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