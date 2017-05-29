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
package com.github.garyaiki.dendrites.algebird.fixtures

import com.twitter.algebird.QTreeSemigroup
import org.scalatest.{Outcome, TestSuite, TestSuiteMixin}
import com.github.garyaiki.dendrites.algebird.buildQTree
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  *
  * @author Gary Struthers
  */
trait QTreeBuilder extends TestSuiteMixin with TestValuesBuilder { this: TestSuite =>

  abstract override def withFixture(test: NoArgTest): Outcome = {
    super.withFixture(test)
  }

  val level = 5
  val q1 = 103.0
  val q2 = 110.0
  val q3 = 115.0
  implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](level)
  val qtBD = buildQTree(bigDecimals)
  val qtBD2 = buildQTree(bigDecimals2)
  implicit val qtBISemigroup = new QTreeSemigroup[BigInt](level)
  val qtBI = buildQTree(bigInts)
  val qtBI2 = buildQTree(bigInts2)
  implicit val qtSemigroup = new QTreeSemigroup[Double](level)
  val qtD = buildQTree(doubles)
  val qtD2 = buildQTree(doubles2)
  implicit val qtFSemigroup = new QTreeSemigroup[Float](level)
  val qtF = buildQTree(floats)
  val qtF2 = buildQTree(floats2)
  implicit val qtISemigroup = new QTreeSemigroup[Int](level)
  val qtI = buildQTree(ints)
  val qtI2 = buildQTree(ints2)
  implicit val qtLSemigroup = new QTreeSemigroup[Long](level)
  val qtL = buildQTree(longs)
  val qtL2 = buildQTree(longs2)
}
