/**

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.garyaiki.dendrites.algebird.agent

import com.twitter.algebird.CMSHasherImplicits._
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.garyaiki.dendrites.algebird.{createCMSMonoid, createCountMinSketch}
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

class CountMinSketchAgentSpec extends WordSpecLike with Matchers with TestValuesBuilder {
  implicit val m = createCMSMonoid[Long]()
  val timeout = Timeout(3000 millis)

  "A CountMinSketchAgent totalCount" should {
    "equal total size" in {
      val cmsAgt = new CountMinSketchAgent[Long]("test Longs")
      val cms0 = createCountMinSketch(longs)
      val updateFuture = cmsAgt.alter(cms0)
      whenReady(updateFuture, timeout) { result => result.totalCount should equal(longs.size) }
    }
  }
}
