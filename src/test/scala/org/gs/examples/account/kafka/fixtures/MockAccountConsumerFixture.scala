/**
  */
package org.gs.examples.account.kafka.fixtures

import org.scalatest._
import org.gs.kafka.MockConsumerFacade

/** @author Gary Struthers
  *
  */

trait MockAccountConsumerFixture extends SuiteMixin { this: Suite =>
  val accountConsumer = MockConsumerFacade
  val consumer = accountConsumer.apply()

  abstract override def withFixture(test: NoArgTest): Outcome = {
    try super.withFixture(test)
    finally {
      consumer.commitSync() // auto commit would occur before processing
      consumer.close()
    }
  }
}
