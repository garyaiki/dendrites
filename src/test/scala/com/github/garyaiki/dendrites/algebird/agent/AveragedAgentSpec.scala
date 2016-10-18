/**
  */
package org.gs.algebird.agent

import com.twitter.algebird._
import org.gs.aggregator._
import org.gs.algebird._
import org.gs.fixtures.TestValuesBuilder
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global

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
      whenReady(updateFuture, timeout) { result =>
        result should equal(avg(bigDecimals))
      }
    }
  }
  
  "AveragedAgent value of BigInts" should {
    "match avg(value)"  in {
      val aa = new AveragedAgent("test BigInts")
      val another = avg(bigInts)
      val updateFuture = aa.alter(another)
      whenReady(updateFuture, timeout) { result =>
        result should equal(avg(bigInts))
      }
    }
  }
  
  "AveragedAgent value of Doubles" should {
    "match avg(value)"  in {
      val aa = new AveragedAgent("test Doubles")
      val another = avg(doubles)
      val updateFuture = aa.alter(another)
      whenReady(updateFuture, timeout) { result =>
        result should equal(avg(doubles))
      }
    }
  }
  
  "AveragedAgent value of Floats" should {
    "match avg(value)"  in {
      val aa = new AveragedAgent("test Floats")
      val another = avg(floats)
      val updateFuture = aa.alter(another)
      whenReady(updateFuture, timeout) { result =>
        result should equal(avg(floats))
      }
    }
  }
  
  "AveragedAgent value of Ints" should {
    "match avg(value)"  in {
      val aa = new AveragedAgent("test Ints")
      val another = avg(ints)
      val updateFuture = aa.alter(another)
      whenReady(updateFuture, timeout) { result =>
        result should equal(avg(ints))
      }
    }
  }
  
  "AveragedAgent value of Longss" should {
    "match avg(value)"  in {
      val aa = new AveragedAgent("test Longs")
      val another = avg(longs)
      val updateFuture = aa.alter(another)
      whenReady(updateFuture, timeout) { result =>
        result should equal(avg(longs))
      }
    }
  }
}
