/**
  */
package org.gs.examples.account.kafka.fixtures

import org.scalatest._
import org.gs.examples.account.kafka.AccountConsumer

/** @author garystruthers
  *
  */

trait AccountConsumerFixture extends SuiteMixin { this: Suite =>
  val accountConsumer = AccountConsumer
  val consumerClient = accountConsumer.apply(AccountConsumer.queue)

  abstract override def withFixture(test: NoArgTest): Outcome = {
    try super.withFixture(test)
    finally {
      consumerClient.consumer.commitSync() // auto commit would occur before processing
      consumerClient.consumer.close()
    }
  }
}
