/**
  */
package org.gs.examples.account

import akka.NotUsed
import akka.stream.scaladsl.Flow

/** Akka Stream Flows used in account examples
  *
  * @author garystruthers
  * @see [[org.gs.examples.account]] package object for the functions these Flows wrap
  */
package object stream {

  /** Flow to extract a list of balances 
    *
    * @see [[org.gs.examples.account]]
    * @example [[org.gs.examples.account.http.stream.ParallelCallAlgebirdFlowSpec]]
    *
    * @tparam A value type in AccBalances
    * @return just the As
    */
    def extractBalancesFlow[A]: Flow[Seq[AnyRef], Seq[A], NotUsed] =
            Flow[Seq[AnyRef]].map(extractBalancesVals[A])
}