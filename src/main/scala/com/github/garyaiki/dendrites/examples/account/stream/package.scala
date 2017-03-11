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
package com.github.garyaiki.dendrites.examples.account

import akka.NotUsed
import akka.stream.scaladsl.Flow

/** Akka Stream Flows used in account examples
  *
  * Extract balances values
  * {{{
  * def source2 = TestSource.probe[Seq[AnyRef]]
  * def sink2 = TestSink.probe[Seq[BigDecimal]]
  *   val (pub2, sub2) = source2
  *    .via(extractBalancesFlow)
  *    .toMat(sink2)(Keep.both).run()
  * }}}
  * @see [[com.github.garyaiki.dendrites.examples.account]] package object for the functions these Flows wrap
  * @author Gary Struthers
  */
package object stream {

  /** Flow to extract a list of balances
    *
    * @tparam A value type in AccBalances
    * @return just the As
    * @see [[com.github.garyaiki.dendrites.examples.account]]
    */
    def extractBalancesFlow[A]: Flow[Seq[AnyRef], Seq[A], NotUsed] = Flow[Seq[AnyRef]].map(extractBalancesVals[A])
}
