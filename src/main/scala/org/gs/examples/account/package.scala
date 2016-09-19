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
package org.gs.examples

import scala.collection.immutable.Set
import scala.reflect.runtime.universe._

/** Functions for AccountType, the case objects that distinguish them and AccountBallances, their
  * optional list of account ids and balances
  *
  * Filter for AccountType
  * {{{
  * if(isAccountType(MoneyMarket) == true)
  * }}}
  * Filter for desired balances types
  * {{{
  * val cb = CheckingAccountBalances[BigDecimal](Some(List((3,3000.3), (33,3300.33), (333,3.33))))
  * if(isAccountBalances(cb) == true)
  * }}}
  * Filter for desired types in an Option[List[(Long, A)]]
  * {{{
  * val filtered = accountBalances.filter(isAccBalances)
  * }}}
  * Extract the 'A' balance values from Option[List[(Long, A)]]
  * {{{
  * def extractBalancesFlow[A]: Flow[Seq[AnyRef], Seq[A], NotUsed] =
  *     Flow[Seq[AnyRef]].map(extractBalancesVals[A])
  * }}}
  * Extract balances from Seq[AccountBalances]
  * {{{
  * def exf: Flow[Seq[AnyRef], Seq[BigDecimal], NotUsed] =
  *     Flow[Seq[AnyRef]].map(extractBalancesVals[BigDecimal])
  * }}}
  * @author Gary Struthers
  */
package object account {

  type AccBalances[A] = Option[List[(Long, A)]]

  val accountTypes: Set[AccountType] = Set(Checking, Savings, MoneyMarket)

  def isAccountBalances(e: Any): Boolean = e.isInstanceOf[AccountBalances]

  def isAccBalances[A: TypeTag](e: Any): Boolean = typeOf[AccBalances[A]] match {
    case t if t =:= typeOf[AccBalances[A]] => true
    case _ => false
  }

  def isAccountType(e: Any): Boolean = e.isInstanceOf[AccountType]

  /** Extract the 'A' balance values from AccBalances[A]
    * @param e Product is a supertype of AccBalances[A], throw exception if it isn't
    * @return List[List[A] call flatten to get List[A]
    */
  def extractBalances[A](e: Product): List[A] = {
    val i = e.productElement(0).asInstanceOf[AccBalances[A]]
    val j = i match {
      case Some(x) => x.map(y => Some(y._2))
      case None    => List(None)
    }
    j.flatten
  }

  /** Extract List[A] balances from AccountBalances
    * @param accountBalances is a Seq of subTypes of AccountBalances
    * @return Seq[List[Product]] call flatten to get List[A]
    */
  def extractBalancesLists[A](accountBalances: Seq[AnyRef]): Seq[List[A]] = {
    val filtered = accountBalances.filter(isAccBalances)
    for (i <- filtered) yield {
      i match {
        case p: Product => extractBalances(p)
      }
    }
  }

  /** Extract balances from Seq[AccountBalances]
    * @param accountBalances is a Seq of subTypes of AccountBalances
    * @return Seq[Product]
    */
  def extractBalancesVals[A](accountBalances: Seq[AnyRef]): Seq[A] = {
    val l = extractBalancesLists(accountBalances)
    l.flatten
  }
}
