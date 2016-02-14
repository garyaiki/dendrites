/**
  */
package org.gs.examples.account.kafka.fixtures

import com.typesafe.config.ConfigFactory
import org.scalatest._
import scala.concurrent.duration.MILLISECONDS
import org.gs.examples.account.kafka.AccountProducer

/** @author garystruthers
  *
  */
trait AccountProducerFixture extends SuiteMixin { this: Suite =>
  val config = ConfigFactory.load()
  val timeout = config.getLong("dendrites.kafka.account.close-timeout")
  val ap = AccountProducer
  
  abstract override def withFixture(test: NoArgTest): Outcome = {
    try super.withFixture(test)
    finally {
      ap.client.producer.flush()
      ap.client.producer.close(timeout, MILLISECONDS)
    }
  }
}
