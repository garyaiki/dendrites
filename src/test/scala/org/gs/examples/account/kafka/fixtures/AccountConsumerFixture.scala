/**
  */
package org.gs.examples.account.kafka.fixtures

import org.scalatest._
import org.gs.examples.account.kafka.AccountConsumerFacade

/** @author garystruthers
  *
  */

trait AccountConsumerFixture extends SuiteMixin { this: Suite =>
  val accountConsumerFacade = AccountConsumerFacade
  val consumer = accountConsumerFacade.apply()

  abstract override def withFixture(test: NoArgTest): Outcome = {
    try super.withFixture(test)
    finally {
      consumer.commitSync() // auto commit would occur before processing
      consumer.close()
    }
  }
}
