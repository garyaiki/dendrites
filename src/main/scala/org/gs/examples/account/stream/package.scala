/**
  */
package org.gs.examples.account

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
  * @author Gary Struthers
  * @see [[org.gs.examples.account]] package object for the functions these Flows wrap
  */
package object stream {

  /** Flow to extract a list of balances 
    *
    * @see [[org.gs.examples.account]]
    *
    * @tparam A value type in AccBalances
    * @return just the As
    */
    def extractBalancesFlow[A]: Flow[Seq[AnyRef], Seq[A], NotUsed] =
            Flow[Seq[AnyRef]].map(extractBalancesVals[A])
}