/**
  */
package org.gs.examples.cassandra.simpleclient.fixtures

import org.scalatest._
import org.gs.examples.cassandra.simpleclient._

/** @author garystruthers
  *
  */
trait SimpleClientFixture extends SuiteMixin { this: Suite =>

  val simpleClient = SimpleClient
  val client = simpleClient.client
  val session = client.session()
  
  abstract override def withFixture(test: NoArgTest): Outcome = {
    try super.withFixture(test)
    finally {
      client.close(session)
    }
  }
}
