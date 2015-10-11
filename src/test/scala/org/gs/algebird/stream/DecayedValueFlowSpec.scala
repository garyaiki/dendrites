/**
  */
package org.gs.algebird.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep }
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.twitter.algebird._
import com.twitter.algebird.CMSHasherImplicits._
import java.net.InetAddress
import language.postfixOps
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs.aggregator._
import org.gs.algebird._
import org.gs.fixtures.{ CaseClassLike, InetAddressesBuilder, TrigUtils }
import scala.collection.immutable.Range
import util.Random

/** @author garystruthers
  *
  */
class DecayedValueFlowSpec extends FlatSpecLike with TrigUtils with InetAddressesBuilder {
  implicit val system = ActorSystem("akka-aggregator")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val monoid = DecayedValueMonoid(0.001)
  val sines = genSineWave(100, 0 to 360)
  val days = Range.Double(0.0, 361.0, 1.0)
  val sinesZip = sines.zip(days)
  val curriedToDecayedValues = curryToDecayedValues(10.0) _
  val decayedValues: Flow[Seq[(Double, Double)], Seq[DecayedValue], Unit] =
    Flow[Seq[(Double, Double)]].map(curriedToDecayedValues)
  val (pub, sub) = TestSource.probe[Seq[(Double, Double)]]
    .via(decayedValues)
    .toMat(TestSink.probe[Seq[DecayedValue]])(Keep.both).run()
  sub.request(1)
  pub.sendNext(sinesZip)
  val dvs = sub.expectNext()
  pub.sendComplete()
  sub.expectComplete()

  "A DecayedValue average with halfLife 10.0" should "exceed the mean at 90º" in {
    assert(dvs(90).average(10.0) > sines.take(90).sum / 90)
  }

  it should "be less than the mean at 180º" in {
    assert(dvs(180).average(10.0) < sines.take(180).sum / 180)
  }

  it should "be less than the mean at 270º" in {
    assert(dvs(270).average(10.0) < sines.take(270).sum / 270)
  }

  it should "be less than mean at 360º" in {
    assert(dvs(360).average(10.0) < sines.take(360).sum / 360)
  }
}