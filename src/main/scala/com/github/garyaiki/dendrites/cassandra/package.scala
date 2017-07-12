/**
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.garyaiki.dendrites

import _root_.akka.actor.ActorSystem
import _root_.akka.event.Logging
import com.datastax.driver.core.{BatchStatement, BoundStatement, CloseFuture, Cluster, Host, KeyspaceMetadata, Metadata,
  QueryLogger, PreparedStatement, ResultSet, Row, Session}
import com.datastax.driver.core.exceptions.NoHostAvailableException
import com.datastax.driver.core.policies.{DCAwareRoundRobinPolicy, DefaultRetryPolicy, LoadBalancingPolicy,
  LoggingRetryPolicy, ReconnectionPolicy, RetryPolicy}
import com.google.common.util.concurrent.ListenableFuture
import com.typesafe.config.ConfigFactory
import java.net.InetAddress
import java.util.{Collection => JCollection, Date => JDate, HashSet => JHashSet, UUID}
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import com.github.garyaiki.dendrites.concurrent.listenableFutureToScala

/** Common functions for Cassandra Java Driver
  *
  * Create single node Cassandra Cluster.
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
  * Asynchronously execute a BoundStatement
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
  implicit val logger = Logging(ActorSystem("dendrites"), getClass)

  /** Create single node Cassandra Cluster.
    *
    * @param node Internet address of initial Cassandra node
    * @return cluster
    *
    * @throws IllegalArgumentException - if no IP address for address could be found
    * @throws [[https://docs.oracle.com/javase/8/docs/api/java/lang/SecurityException.html SecurityException]]
    * @throws SecurityException - if a security manager is present and permission denied
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/Cluster.Builder.html Builder]]
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/Cluster.html Cluster]]
    * @see [[https://docs.oracle.com/javase/8/docs/api/java/lang/IllegalArgumentException.html IllegalArgumentException]]
    */
  def createCluster(node: String): Cluster = Cluster.builder.addContactPoint(node).build

  /** Create Cluster with multiple host nodes and a RetryPolicy
    *
    * @param nodes Cassandra nodes
    * @param policy RetryPolicy
    * @return Cluster
    *
    * @see [[https://docs.oracle.com/javase/8/docs/api/index.html?java/net/InetAddress.html InetAddress]]
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/policies/RetryPolicy.html RetryPoly]]
    */
  def createCluster(nodes: JCollection[InetAddress], policy: RetryPolicy): Cluster = {
    Cluster.builder.addContactPoints(nodes).withRetryPolicy(policy).build
  }

  /** Create Cluster with multiple host nodes, a RetryPolicy, and a ReconnectionPolicy
    *
    * Datastax:"The default ExponentialReconnectionPolicy policy is usually adequate."
    *
    * @param nodes Cassandra nodes
    * @param policy RetryPolicy
    * @param recP: ReconnectionPolicy
    * @return Cluster
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/policies/ExponentialReconnectionPolicy.html ExponentialReconnectionPolicy]]
    */
  def createCluster(nodes: JCollection[InetAddress], policy: RetryPolicy, recP: ReconnectionPolicy): Cluster = {
    Cluster.builder.addContactPoints(nodes).withRetryPolicy(policy).withReconnectionPolicy(recP).build
  }

  /** Log cluster's metadata. ForDebugging
    *
    * @param cluster
    *
    * @throws NoHostAvailableException - if Cluster uninitialized and no host reachable
    * @throws AuthenticationException - if authentication error contacting initial contact points.
    * @throws IllegalStateException - if Cluster closed prior to calling. Can occur either directly
    * through close() or closeAsync(), or from an error while initializing the Cluster.
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/Metadata.html Metadata]]
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/NoHostAvailableException.html NoHostAvailableException]]
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/AuthenticationException.html AuthenticationException]]
    * @see [[https://docs.oracle.com/javase/8/docs/api/java/lang/IllegalStateException.html IllegalStateException]]
    */
  def logMetadata(cluster: Cluster): Unit = {
    val metadata = cluster.getMetadata
    logger.debug("Connected to cluster:{}", metadata.getClusterName)
    val hosts = metadata.getAllHosts
    val it = hosts.iterator.asScala
    it.foreach(h => logger.debug("Datacenter:{} host:{} rack:{}", h.getDatacenter, h.getAddress, h.getRack))
  }

  /** Enable logging of RegularStatement, BoundStatement, BatchStatement queries
    *
    * @param cluster
    *
    * @throws IllegalArgumentException - if builder unable to build due to incorrect settings.
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/QueryLogger.html QueryLogger]]
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/QueryLogger.Builder.html QueryLogger.Builder]]
    *
    */
  def registerQueryLogger(cluster: Cluster): Unit = {
    val queryLogger = QueryLogger.builder.withConstantThreshold(10000).withMaxQueryStringLength(256).build
    cluster.register(queryLogger)
  }

  /** Create a simple LoadBalancing policy
    *
    * @param localDc Local Datacenter name
    * @return LoadBalancingPolicy
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/policies/LoadBalancingPolicy.html LoadBalancingPolicy]]
    */
  def createLoadBalancingPolicy(localDc: String): LoadBalancingPolicy = {
    DCAwareRoundRobinPolicy.builder.withLocalDc(localDc).build
  }

  /** Initialize LoadBalancingPolicy
    *
    * @param cluster
    * @param ldBalPolicy: LoadBalancingPolicy
    *
    * @throws NoHostAvailableException - Cluster uninitialized and no host reachable
    * @throws AuthenticationException - authentication error contacting initial contact points.
    * @throws IllegalStateException - Cluster closed prior to calling. Can occur either directly
    * (through close() or closeAsync()), or an error initializing Cluster.
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/NoHostAvailableException.html NoHostAvailableException]]
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/AuthenticationException.html AuthenticationException]]
    * @see [[https://docs.oracle.com/javase/8/docs/api/java/lang/IllegalStateException.html IllegalStateException]]
    *
    */
  def initLoadBalancingPolicy(cluster: Cluster, ldBalPolicy: LoadBalancingPolicy): Unit = {
    val hosts = cluster.getMetadata.getAllHosts
    ldBalPolicy.init(cluster, hosts)
  }

  /** Connect to Cassandra. Return a Session which is thread safe and may last app's lifetime
    *
    * @param cluster
    * @param keyspace Specify only if keyspace exists
    * @return Session
    *
    * @throws NoHostAvailableException - Cluster uninitialized and no host reachable
    * @throws AuthenticationException - authentication error contacting initial contact points.
    * @throws IllegalStateException - Cluster closed prior to calling. Can occur either directly
    * @throws InvalidQueryException - syntactically correct but invalid
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/InvalidQueryException.html InvalidQueryException]]
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
    *
    * @throws UnsupportedFeatureException - protocol version 1 and feature not supported
    * @throws NoHostAvailableException - no host can be contacted
    * @throws QueryExecutionException - query triggered execution exception
    * @throws QueryValidationException - query is invalid, syntax error, unauthorized
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/UnsupportedFeatureException.html UnsupportedFeatureException]]
    *
    */
  def createSchema(session: Session, schema: String, strategy: String, repCount: Int): ResultSet = {
    val rsf = session.executeAsync("CREATE KEYSPACE IF NOT EXISTS " + schema + " WITH replication" +
      "= {'class': '" + strategy + "', 'replication_factor':" + repCount + "};")
    rsf.getUninterruptibly
  }

  /** Verbose description of keyspaces with their tables and queries
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.0/index.html?com/datastax/driver/core/querybuilder/QueryBuilder.html]]
    * @param session
    * @return all keyspaces metadata for session
    */
  def getKeyspacesMetadata(session: Session): String = {
    val ks = session.getCluster.getMetadata.getKeyspaces.asScala
    val sb = new StringBuilder(" keyspaces metadata:")
    ks foreach(md => {
      sb.append(md.exportAsString)
      sb.append(", ")
    })
    sb.drop(2)
    sb.toString
  }

  /** Verbose description of keyspaces
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.0/index.html?com/datastax/driver/core/querybuilder/QueryBuilder.html]]
    * @param session
    * @return keyspaces info
    */
  def getKeyspacesStrings(session: Session): String = {
    val ks = session.getCluster.getMetadata.getKeyspaces.asScala
    val sb = new StringBuilder(" keyspaces :")
    ks foreach(md => {
      sb.append(md.toString)
      sb.append(", ")
    })
    sb.drop(2)
    sb.toString
  }

  def getKeyspacesNames(session: Session): String = {
    val ks = session.getCluster.getMetadata.getKeyspaces.asScala
    val sb = new StringBuilder(" keyspaces :")
    ks foreach(md => {
      sb.append(md.getName)
      sb.append(", ")
    })
    sb.drop(2)
    sb.toString
  }
  /** Create a PreparedStatement to return all rows of a table
    *
    * @param session
    * @param schema
    * @param table
    * @return PreparedStatement Cassandra DB has prepared
    *
    * @throws NoHostAvailableException - no host can be contacted
    */
  @deprecated("May undepricate if issue fixed https://issues.apache.org/jira/browse/CASSANDRA-10786", "dendrites 0.5")
  def selectAll(session: Session, schema: String, table: String): PreparedStatement = {
    session.prepare("SELECT * FROM " + schema + "." + table + ";")
  }

  /** Asynchronously execute a BoundStatement but getUninterruptibly blocks, use blocking-dispatcher
    *
    * [[com.github.garyaiki.dendrites.cassandra.stream.CassandraSink]] shows fully asynchronous executeAsync with Future
    *
    * @param session
    * @param bndStmt
    * @return ResultSet on Success
    *
    * @throws UnsupportedFeatureException - protocol version 1 and feature not supported
    * @throws NoHostAvailableException - no host can be contacted
    * @throws QueryExecutionException - query execution exception
    * @throws QueryValidationException - query is invalid,syntax error, unauthorized
    *
    */
  def executeBoundStmt(session: Session, bndStmt: BoundStatement): ResultSet = {
    val resultSetFuture = session.executeAsync(bndStmt)
    resultSetFuture.getUninterruptibly
  }

  /** Get every row in a result set
    *
    * @param resultSet
    * @return Seq[Row]
    */
  def getAllRows(resultSet: ResultSet): Seq[Row] = resultSet.all.asScala.toSeq

  /** Get column names for a row. Can be used to log a failed conditional update or insert
    *
    * @param row
    */
  def getRowColumnNames(row: Row): StringBuilder = {
    val sb = new StringBuilder("Cassandra row column names:")
    if(row == null) {
      sb.append("row is null")
    } else {
      val colDefs = row.getColumnDefinitions.asList.asScala
      colDefs foreach {x => sb.append(x.getName); sb.append(',');}
      sb.dropRight(1)
    }
  }

  /** Handle conditional insert, update, delete. Conditional statements return a ResultSet with a single Row. If
    * statement successfully applied rs.wasApplied is true. If failed, call a user defined function to log or throw an
    * exception. Insert, update, delete statements that aren't conditional don't return a row
    *
    * @param rowAction
    * @param rs
    * @return Some[Row] if conditional statement failed
    */
  def getConditionalError(rowAction: Row => Unit)(rs: ResultSet): Option[Row] = {
    if(rs.wasApplied) None else {
      val one = rs.one
      one match {
        case null => None
        case _ => {
          rowAction(one)
          Some(one)
        }
      }
    }
  }

  def sessionLogInfo(session: Session): String = {
    val sb = new StringBuilder("session logged keyspace:")
    sb.append(session.getLoggedKeyspace)
    sb.append(" cluster:")
    sb.append(session.getCluster.getClusterName)
    sb.append(" session isClosed:")
    sb.append(session.isClosed)
    val state = session.getState
    val hosts = state.getConnectedHosts.asScala
    hosts foreach{ host => {
        sb.append(" host state:")
        sb.append(host.getState)
        sb.append(", host inFlightQueries:")
        sb.append(state.getInFlightQueries(host))
        sb.append(", host openConnections:")
        sb.append(state.getOpenConnections(host))
        sb.append(", host trashedConnections:")
        sb.append(state.getTrashedConnections(host))
        sb.append(", ")
      }
    }
    sb.drop(2)
    sb.toString
  }
  /** Add host errors to NoHostAvailableException message
    *
    * @param e NoHostAvailableException
    * @return augmented error message
    */
  def noHostAvailableExceptionMsg(e: NoHostAvailableException): String = {
    val sb = new StringBuilder(e.getMessage)
    sb.append(" host errors:")
    val errors = e.getErrors.asScala
    errors foreach { x =>
      {
        sb.append(x._1.getHostString)
        sb.append(" msg:")
        sb.append(x._2.getMessage)
        sb.append(", ")
      }
    }
    sb.toString.dropRight(2)
  }

  /** drop schema (keyspace)
    *
    * @param session
    * @param schema
    *
    * @throws NoHostAvailableException - no host can be contacted
    * @throws QueryExecutionException - query execution exception
    * @throws QueryValidationException - query is invalid,syntax error, unauthorized
    *
    */
  def dropSchema(session: Session, schema: String): Unit = session.execute("DROP KEYSPACE IF EXISTS " + schema)

  /** Asynchronously close Session and Cluster. Converts Cassandra's Java Futures into Scala Futures
    *
    * @param session
    * @param cluster
    * @param force hurry up flag
    * @param implicit ec ListenableFuture must have an ExecutionContext that is a Java Executor
    *
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/CloseFuture.html CloseFuture]]
    * @see [[http://google.github.io/guava/releases/22.0/api/docs/com/google/common/util/concurrent/ListenableFuture.html ListenableFuture]]
    * @see [[http://www.scala-lang.org/api/current/scala/concurrent/ExecutionContextExecutor.html ExecutionContextExecutor]]
    *
    */
  def close(session: Session, cluster: Cluster, force: Boolean = false)(implicit ec: ExecutionContextExecutor): Unit = {
    val sessCloseF = session.closeAsync()
    val clusCloseF = cluster.closeAsync()
    if(force) {
      sessCloseF.force()
      clusCloseF.force()
    }
    val scalaSessF = listenableFutureToScala[Unit](sessCloseF.asInstanceOf[ListenableFuture[Unit]])
    scalaSessF onComplete {
      case Success(x) => logger.debug("session closed")
      case Failure(t) => logger.error(t, "session closed failed {}", t.getMessage)
    }
    val scalaClusF = listenableFutureToScala[Unit](clusCloseF.asInstanceOf[ListenableFuture[Unit]])
    scalaClusF onComplete {
      case Success(x) => logger.debug("cluster closed")
      case Failure(t) => logger.error(t, "failed cluster close {}", t.getMessage)
    }
  }
}
