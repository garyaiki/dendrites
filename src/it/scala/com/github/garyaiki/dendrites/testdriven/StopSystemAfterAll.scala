package com.github.garyaiki.dendrites.testdriven

import org.scalatest.{Suite, BeforeAndAfterAll}
import akka.testkit.TestKit

trait StopSystemAfterAll extends BeforeAndAfterAll {
  this: TestKit with Suite =>
    override protected def afterAll() {
      super.afterAll()
      system.terminate()
    }

}