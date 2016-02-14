/**
  */
package org.gs.examples.cassandra.boundstmtclient.fixtures

import org.scalatest._
import org.gs.examples.cassandra.boundstmtclient.BoundStmtClient

/** @author garystruthers
  *
  */
trait BoundStmtClientFixture extends SuiteMixin { this: Suite =>

  val boundStmtClient = BoundStmtClient
  val client = boundStmtClient.client
  val session = client.session()
  
  abstract override def withFixture(test: NoArgTest): Outcome = {
    try super.withFixture(test)
    finally {
      client.close(session)
    }
  }
}
