package org.gs.algebird.agent

import com.twitter.algebird.CMSHasherImplicits._
import org.gs.algebird._
import org.gs.fixtures.TestValuesBuilder
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global

class CountMinSketchAgentSpec extends WordSpecLike with Matchers with TestValuesBuilder {
  implicit val m = createCMSMonoid[Long]()
  val timeout = Timeout(3000 millis)

  "A CountMinSketchAgent totalCount" should {
    "equal total size" in {
      val aa = new CountMinSketchAgent[Long]("test Longs")
      val cms0 = createCountMinSketch(longs)
      val updateFuture = aa.alter(cms0)
      whenReady(updateFuture, timeout) { result =>
        result.totalCount should equal(longs.size)
      }
    }
  }

}