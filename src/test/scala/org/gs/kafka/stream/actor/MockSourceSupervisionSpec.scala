/** Copyright 2016 Gary Struthers

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
package org.gs.kafka.stream.actor

import akka.actor.{ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.stream.OverflowStrategy
import akka.stream.OverflowStrategy.fail
import akka.stream.scaladsl.{RunnableGraph, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorAttributes, Supervision}
import akka.testkit.{TestKit}
import java.util.MissingResourceException
import org.scalatest.{Matchers, WordSpecLike}
import scala.collection.mutable.ArrayBuffer
import org.gs.examples.account.GetAccountBalances
import org.gs.kafka.stream.MockKafkaSource
import org.gs.stream.actor.CallStreamSupervisor
import org.gs.stream.actor.CallStreamSupervisor.props

/**
  *
  * @author Gary Struthers
  */
class MockSourceSupervisionSpec extends TestKit(ActorSystem("test")) with WordSpecLike
        with Matchers {

  implicit val logger: LoggingAdapter = system.log
  val nums = Seq("0","1","2","3","4","5","6","7","8","9", "10", "11", "12")
  val source = Source.queue[String](10, OverflowStrategy.fail)
  val sink = Sink.ignore

  "A MockSourceSupervisior" should {
    "process all messages in child actor" in {
      val iter: Iterator[String] = nums.toIterator

      val rg: RunnableGraph[SourceQueueWithComplete[String]] = source.to(sink)

      val proxy = system.actorOf(CallStreamSupervisor.props(rg))
      while (iter.hasNext)  proxy ! iter.next()
    }

    "restart child actor after elem throws exception" in {
      val iter: Iterator[String] = nums.toIterator
      val sb = new StringBuilder()
      val rg: RunnableGraph[SourceQueueWithComplete[String]] = source.map{
        elem => if(elem == "2") {
            throw new NullPointerException("throw exception when elem = 2 {}, elem")
          } else elem
      }.map{ elem => sb.append(elem); elem }.to(sink)
      
      val proxy = system.actorOf(CallStreamSupervisor.props(rg))
      while (iter.hasNext) {
        proxy ! iter.next()
        Thread.sleep(50)
      }
      sb.toString shouldBe "01456789101112"
    }
    "stop child actor after exceeding supervisor maxNrOfRetries" in {
      val iter: Iterator[String] = nums.toIterator
      val sb = new StringBuilder()
      val rg: RunnableGraph[SourceQueueWithComplete[String]] = source.map{
        elem => elem match {
          case "2" | "4" | "6" | "8" => throw new NullPointerException("throw when elem = {}, elem")
          case _ => elem
        }
      }.map{ elem => sb.append(elem); elem }.to(sink)
      
      val proxy = system.actorOf(CallStreamSupervisor.props(rg))
      while (iter.hasNext) {
        proxy ! iter.next()
        Thread.sleep(50)
      }
      sb.toString shouldBe "01"
    }
  }
}
