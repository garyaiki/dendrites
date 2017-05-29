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

import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.garyaiki.dendrites.algebird.avg
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  *
  * @author Gary Struthers
  *
  */
class AveragedAgentSpec extends WordSpecLike with Matchers with TestValuesBuilder {

  val timeout = Timeout(3000 millis)

  "AveragedAgent value of BigDecimals" should {
    "match avg(value)"  in {
      val aa = new AveragedAgent("test BigDecimals")
      val another = avg(bigDecimals)
      val updateFuture = aa.alter(another)
      whenReady(updateFuture, timeout) { result => result should equal(avg(bigDecimals)) }
    }
  }

  "AveragedAgent value of BigInts" should {
    "match avg(value)"  in {
      val aa = new AveragedAgent("test BigInts")
      val another = avg(bigInts)
      val updateFuture = aa.alter(another)
      whenReady(updateFuture, timeout) { result => result should equal(avg(bigInts)) }
    }
  }

  "AveragedAgent value of Doubles" should {
    "match avg(value)"  in {
      val aa = new AveragedAgent("test Doubles")
      val another = avg(doubles)
      val updateFuture = aa.alter(another)
      whenReady(updateFuture, timeout) { result => result should equal(avg(doubles)) }
    }
  }

  "AveragedAgent value of Floats" should {
    "match avg(value)"  in {
      val aa = new AveragedAgent("test Floats")
      val another = avg(floats)
      val updateFuture = aa.alter(another)
      whenReady(updateFuture, timeout) { result => result should equal(avg(floats)) }
    }
  }

  "AveragedAgent value of Ints" should {
    "match avg(value)"  in {
      val aa = new AveragedAgent("test Ints")
      val another = avg(ints)
      val updateFuture = aa.alter(another)
      whenReady(updateFuture, timeout) { result => result should equal(avg(ints)) }
    }
  }

  "AveragedAgent value of Longss" should {
    "match avg(value)"  in {
      val aa = new AveragedAgent("test Longs")
      val another = avg(longs)
      val updateFuture = aa.alter(another)
      whenReady(updateFuture, timeout) { result => result should equal(avg(longs)) }
    }
  }
}
