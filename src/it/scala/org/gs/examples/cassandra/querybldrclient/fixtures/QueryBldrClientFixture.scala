/**
  */
package org.gs.examples.cassandra.querybldrclient.fixtures

import org.scalatest._
import org.gs.examples.cassandra.querybldrclient.QueryBldrClient


/** @author garystruthers
  *
  */
trait QueryBldrClientFixture extends SuiteMixin { this: Suite =>

  val queryBldrClient = QueryBldrClient
  val client = queryBldrClient.client
  val session = client.session()
  
  abstract override def withFixture(test: NoArgTest): Outcome = {
    try super.withFixture(test)
    finally {
      client.close(session)
    }
  }
}
