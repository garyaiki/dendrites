/**
  */
package org.gs.algebird.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep }
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs.algebird.fixtures.BloomFilterBuilder
import org.gs.algebird._
import org.gs._
import com.twitter.algebird._
import scala.concurrent.duration._

/** @author garystruthers
  *
  */
class BloomFilterFlowSpec extends FlatSpecLike with BloomFilterBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  val properFilter: Flow[String, String, Unit] =
    Flow[String].filter { x => properBF.contains(x).isTrue }

  val properMat = TestSource.probe[String]
    .via(properFilter)
    .toMat(TestSink.probe[String])(Keep.both)

  "A properNames BloomFilter Flow" should "have 0 false negatives" in {
    val (pub, sub) = properMat.run()
    sub.request(properTestWords.size)
    for (i <- properTestWords) {
      pub.sendNext(i)
      sub.expectNext(i)
    }
    pub.sendComplete()
    sub.expectComplete()
  }

  it should "have fewer false positives than the false positives probability" in {
    val fpProb: Double = 0.02
    val (pub, sub) = properMat.run()
    sub.request(properFalseWords.size)

    for (i <- properFalseWords) pub.sendNext(i)

    val falsePositives = sub.receiveWithin(FiniteDuration(1, SECONDS), properFalseWords.size)
    assert(falsePositives.size <= properNames.size * fpProb)
  }

  val connectivesFilter: Flow[String, String, Unit] =
    Flow[String].filter { x => connectivesBF.contains(x).isTrue }

  val connectivesMat = TestSource.probe[String]
    .via(connectivesFilter)
    .toMat(TestSink.probe[String])(Keep.both)

  "A connectives BloomFilter Flow" should "have 0 false negatives" in {
    val (pub, sub) = connectivesMat.run()
    sub.request(connectivesTestWords.size)
    for (i <- connectivesTestWords) {
      pub.sendNext(i)
      sub.expectNext(i)
    }
    pub.sendComplete()
    sub.expectComplete()
  }

  it should "have fewer false positives than the false positives probability" in {
    val fpProb: Double = 0.04
    val (pub, sub) = connectivesMat.run()
    sub.request(connectivesFalseWords.size)

    for (i <- connectivesFalseWords) pub.sendNext(i)
    val falsePositives = sub.receiveWithin(FiniteDuration(1, SECONDS), connectivesFalseWords.size)
    assert(falsePositives.size <= connectives.size * fpProb)

  }

  val wordsFilter: Flow[String, String, Unit] =
    Flow[String].filter { x => wordsBF.contains(x).isTrue }

  val wordsMat = TestSource.probe[String]
    .via(wordsFilter)
    .toMat(TestSink.probe[String])(Keep.both)

  "A words BloomFilter Flow" should "have 0 false negatives" in {
    val (pub, sub) = wordsMat.run()
    sub.request(wordsTestWords.size)
    for (i <- wordsTestWords) {
      pub.sendNext(i)
      sub.expectNext(i)
    }
    pub.sendComplete()
    sub.expectComplete()
  }

  it should "have fewer false positives than the false positives probability" in {
    val fpProb: Double = 0.04
    val (pub, sub) = wordsMat.run()
    sub.request(wordsFalseWords.size)
        for (i <- wordsFalseWords) pub.sendNext(i)
    val falsePositives = sub.receiveWithin(FiniteDuration(1, SECONDS), wordsFalseWords.size)
    assert(falsePositives.size < words.size * fpProb)
  }
}
