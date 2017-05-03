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
package com.github.garyaiki.dendrites.cassandra.fixtures

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import com.datastax.driver.core.{Cluster, Session}
import org.scalatest.{Outcome, TestSuite, TestSuiteMixin}
import scala.concurrent.ExecutionContext
import com.github.garyaiki.dendrites.cassandra.CassandraConfig
import com.github.garyaiki.dendrites.cassandra.{close, connect, createSchema, dropSchema}

trait BeforeAfterAllBuilder extends TestSuiteMixin { this: TestSuite =>

  abstract override def withFixture(test: NoArgTest): Outcome = { super.withFixture(test) }

  implicit val system = ActorSystem("dendrites")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  var schema: String = null
  var cluster: Cluster = null
  var session: Session = null

  def createClusterSchemaSession(config: CassandraConfig, repCount: Int): Unit = {
    cluster = buildCluster(config)
    session = connect(cluster)
    schema = config.keySpace
    val strategy = config.replicationStrategy
    createSchema(session, schema, strategy, repCount)
  }

  def dropSchemaCloseSessionCluster(): Unit = {
    dropSchema(session, schema)
    close(session, cluster)
  }
}