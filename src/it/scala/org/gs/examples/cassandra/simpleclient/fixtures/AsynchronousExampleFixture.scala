/**
  */
package org.gs.examples.cassandra.simpleclient.fixtures

import org.scalatest._
import org.gs.examples.cassandra.simpleclient._


/** @author garystruthers
  *
  */
trait AsynchronousExampleFixture extends SuiteMixin { this: Suite =>

  val asyncExample = AsynchronousExample
  val client = asyncExample.client
  val session = client.session()
  
  abstract override def withFixture(test: NoArgTest): Outcome = {
    try super.withFixture(test)
    finally {
      client.close(session)
    }
  }
}
