package org.gs

import _root_.akka.actor.ActorSystem
import _root_.akka.event.Logging
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.{BatchStatement, BoundStatement, CloseFuture, Cluster, Host}
import com.datastax.driver.core.{Metadata, QueryLogger, PreparedStatement, ResultSet, Row, Session}
import com.datastax.driver.core.policies.{DCAwareRoundRobinPolicy, DefaultRetryPolicy}
import com.datastax.driver.core.policies.{LoadBalancingPolicy, LoggingRetryPolicy, RetryPolicy}
import com.google.common.util.concurrent.ListenableFuture
import com.typesafe.config.ConfigFactory
import java.net.InetAddress
import java.util.{Collection => JCollection, Date => JDate, HashSet => JHashSet, UUID}
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import org.gs.concurrent.listenableFutureToScala

/** CassandraConfig configuration trait for Cassandra Java Driver
  *
  * Create Cassandra Cluster (for development).
  * {{{
  * val config = ConfigFactory.load()
  * val ipAddress = config.getString("dendrites.cassandra.ipAddress")
  * val cluster = createCluster(ipAddress)
  * }}}
  * Create Cluster with multiple host nodes and a RetryPolicy
  * {{{
  * val addresses = myConfig.getInetAddresses()
  * val retryPolicy = new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE)
  * cluster = createCluster(addresses, retryPolicy)
  * }}}
  * Log cluster's metadata.
  * {{{
  * logMetadata(cluster)
  * }}}
  * Bind Cassandra's QueryLogger to cluster
  * {{{
  * registerQueryLogger(cluster)
  * }}}
  * Create a simple LoadBalancing policy
  * {{{
  * val myConfig = PlaylistSongConfig
  * val lbp = createLoadBalancingPolicy(myConfig.localDataCenter)
  * }}}
  * Initialize LoadBalancingPolicy
  * {{{
  * initLoadBalancingPolicy(cluster, lbp)
  * }}}
  * Connect to Cassandra. Return a Session which is thread safe and may last app's lifetime
  * {{{
  * session = connect(cluster)
  * }}}
  * Create a Keyspace
  * {{{
  * val schema = myConfig.keySpace
  * val strategy = myConfig.replicationStrategy
  * val createSchemaRS = createSchema(session, schema, strategy, 3)
  * }}}
  * Create a PreparedStatement to return all rows of a table
  * {{{
  * val plPreStmt = selectAll(session, schema, Playlists.table)
  * }}}
  * Asychronously execute a BoundStatement
  * {{{
  * val selAllRS = executeBoundStmt(session, new BoundStatement(plPreStmt))
  * }}}
  * Get every row in a result set
  * {{{
  * val allRows = getAllRows(selAllRS)
  * }}}
  * Drop schema
  * {{{
  * dropSchema(session, schema)
  * }}}
  * Asynchronously close Session and Cluster. Turns Cassandra's Java Futures into Scala Futures
  * {{{
  * close(session, cluster)
  * }}}
  */  
package object cassandra {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)

  /** Create Cassandra Cluster (for development).
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.0/com/datastax/driver/core/Cluster.Builder.html Builder]]
    * @see [[http://docs.datastax.com/en/drivers/java/3.0/com/datastax/driver/core/Cluster.html Cluster]]
    * @param node Internet address of initial Cassandra node 
    * @return cluster
    */
  def createCluster(node: String): Cluster = Cluster.builder().addContactPoint(node).build()
  
  /** Create Cluster with multiple host nodes and a RetryPolicy
    *
    * @see [[https://docs.oracle.com/javase/8/docs/api/index.html?java/net/InetAddress.html InetAddress]]
    * @see [[http://docs.datastax.com/en/drivers/java/3.0/com/datastax/driver/core/policies/RetryPolicy.html RetryPolicy]]
    * @param nodes Cassandra nodes
    * @param policy RetryPolicy
    * @return Cluster
    */
  def createCluster(nodes: JCollection[InetAddress], policy: RetryPolicy): Cluster = {
    Cluster.builder().addContactPoints(nodes).withRetryPolicy(policy).build()
  }

  /** Log cluster's metadata.
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.0/com/datastax/driver/core/Metadata.html Metadata]]
    * @param cluster
    */
  def logMetadata(cluster: Cluster): Unit = {
    val metadata = cluster.getMetadata()
    logger.debug(s"Connected to cluster:${metadata.getClusterName}")
    val hosts = metadata.getAllHosts()
    val it = hosts.iterator()
    while(it.hasNext()) {
      val h = it.next()
      logger.debug(s"Datacenter:${h.getDatacenter()} host:${h.getAddress} rack:${h.getRack()}") 
    }    
  }

  /** Enable logging of RegularStatement, BoundStatement, BatchStatement queries
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.0/com/datastax/driver/core/QueryLogger.html QueryLogger]]
    * @param cluster
    */
  def registerQueryLogger(cluster: Cluster): Unit = {
    val queryLogger = QueryLogger.builder()
    .withConstantThreshold(10000)
    .withMaxQueryStringLength(256)
    .build()

    cluster.register(queryLogger)
  }

  /** Create a simple LoadBalancing policy
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.0/com/datastax/driver/core/policies/LoadBalancingPolicy.html LoadBalancingPolicy]]
    * @param localDc Local Datacenter name
    * @return
    */
  def createLoadBalancingPolicy(localDc: String): LoadBalancingPolicy = {
    DCAwareRoundRobinPolicy.builder().withLocalDc(localDc).build()
  }

  /** Initialize LoadBalancingPolicy
    *
    * @param cluster
    * @param ldBalPolicy
    */
  def initLoadBalancingPolicy(cluster: Cluster, ldBalPolicy: LoadBalancingPolicy): Unit = {
    val hosts = cluster.getMetadata().getAllHosts()
    ldBalPolicy.init(cluster, hosts)
  }

  /** Connect to Cassandra. Return a Session which is thread safe and may last app's lifetime
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.0/com/datastax/driver/core/Session.html Session]]
    * @param cluster
    * @param keyspace Specify only if keyspace exists 
    * @return Session
  */
  def connect(cluster: Cluster, keyspace: Option[String] = None): Session = {
    keyspace match {
      case Some(x) => cluster.connect(x)
      case None => cluster.connect()
    }
  }

  /** Create a Keyspace
    *
    * @param session
    * @param schema aka KEYSPACE
    * @param strategy SimpleStrategy for 1 datacenter NetowrkTopologyStrategy for more datacenters
    * @param repCount number of copies of data
    * @return an empty ResultSet on Success
    */
  def createSchema(session: Session, schema: String, strategy: String, repCount: Int): ResultSet = {
    val rsf = session.executeAsync("CREATE KEYSPACE IF NOT EXISTS " + schema + " WITH replication"
        + "= {'class': '" + strategy + "', 'replication_factor':" + repCount + "};")
    rsf.getUninterruptibly() 
  }
  
  /** Create a PreparedStatement to return all rows of a table
    *
    * @param session
    * @param schema
    * @param table
    * @return PreparedStatement Cassandra DB has prepared
    */
  def selectAll(session: Session, schema: String, table: String): PreparedStatement = {
      session.prepare("SELECT * FROM " + schema + "." + table + ";")
  }

  /** Asychronously execute a BoundStatement
    *
    * @param session
    * @param bndStmt with values previously bound
    * @return ResultSet on Success
    */
  def executeBoundStmt(session: Session, bndStmt: BoundStatement): ResultSet = {
    val resultSetFuture = session.executeAsync(bndStmt)
    resultSetFuture.getUninterruptibly()
  }

  /** Get every row in a result set
    *
    * @param resultSet
    * @return Seq[Row]
    */
  def getAllRows(resultSet: ResultSet): Seq[Row] = resultSet.all().toSeq

  /** drop schema (aka keyspace)
    *
    * @param session
    * @param schema
    */
  def dropSchema(session: Session, schema: String): Unit = {
    session.execute("DROP KEYSPACE IF EXISTS " + schema)
  }

  /** Asynchronously close Session and Cluster. Turns Cassandra's Java Futures into Scala Futures
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.0/com/datastax/driver/core/CloseFuture.html CloseFuture]]
    * @see [[http://google.github.io/guava/releases/19.0/api/docs/com/google/common/util/concurrent/ListenableFuture.html ListenableFuture]]
    * @param session
    * @param cluster
    * @param force hurry up flag
    */
  def close(session: Session, cluster: Cluster, force: Boolean = false): Unit = {
    val sessCloseF = session.closeAsync()
    val clusCloseF = cluster.closeAsync()
    if(force) {
      sessCloseF.force()
      clusCloseF.force()
    }
    val scalaSessF = listenableFutureToScala[Unit](sessCloseF.asInstanceOf[ListenableFuture[Unit]])
    scalaSessF onComplete {
      case Success(x) => logger.debug("session closed")
      case Failure(t) => logger.error(t, "session closed failed {}", t.getMessage())
    }
    val scalaClusF = listenableFutureToScala[Unit](clusCloseF.asInstanceOf[ListenableFuture[Unit]])
    scalaClusF onComplete {
      case Success(x) => logger.debug("cluster closed")
      case Failure(t) => logger.error(t, "cluster closed failed {}", t.getMessage())
    }
  }
}