/**
  */
package org.gs.examples.account

import akka.stream.scaladsl.Flow
//import com.twitter.algebird._
//import org.gs.algebird.typeclasses.QTreeLike
//import scala.reflect.runtime.universe._

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
    def extractBalancesFlow[A]: Flow[Seq[AnyRef], Seq[A], Unit] =
            Flow[Seq[AnyRef]].map(extractBalancesVals[A])
}