/**
  */
package org.gs.examples.account.kafka.fixtures

import org.scalatest._
import org.gs.kafka.MockConsumerFacade

/** @author Gary Struthers
  *
  */

trait MockAccountConsumerFixture extends SuiteMixin { this: Suite =>
  val mockConsumerFacade = MockConsumerFacade
  val mockConsumer = mockConsumerFacade.apply()

  abstract override def withFixture(test: NoArgTest): Outcome = {
    try super.withFixture(test)
    finally {
      mockConsumer.commitSync() // auto commit would occur before processing
      mockConsumer.close()
    }
  }
}
