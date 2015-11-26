package org.gs.algebird.agent

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.reflect.runtime.universe.TypeTag

import akka.agent.Agent
import com.twitter.algebird.{QTree, QTreeSemigroup}

import org.gs.algebird.{buildQTree, buildQTrees}
import org.gs.algebird.AlgebirdConfigurer.qTreeLevel
import org.gs.algebird.typeclasses.QTreeLike

/** Shared state for Qtree
  *
  * @see [[http://doc.akka.io/api/akka/current/#akka.agent.Agent]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTreeSemigroup]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.SeqMonoid]]
  * @example [[org.gs.algebird.agent.QTreeAgentSpec]]
  * @author garystruthers
  *
  * @tparam A: QTreeLike either BigDecimal, BigInt, Double, Float, Int or Long
  * @param name
  * @param xs optional initial values
  * @param ec implicit execution context for future
  * @param sg implicit QTreeSemigroup for adding
  *
  */
class QTreeAgent[A: QTreeLike : TypeTag](
    val name: String = "",
    level: Int = qTreeLevel,
    xs: Option[Seq[A]] = None)
  (implicit ec: ExecutionContext, sg: QTreeSemigroup[A]) {

  val zero: QTree[A] = implicitly[QTreeLike[A]].apply(sg.underlyingMonoid.zero)

  val agent = xs match {
    case None    => Agent(zero)
    case Some(xs) => Agent(buildQTree[A](xs))
  }

  /** Update agent with sequence of values
    *
    * @param xs Seq of BigDecimal, BigInt, Double, Float, Int or Long
    * @return future of new QTree after this and all pending updates
    */
  def alter(xs: Seq[A]): Future[QTree[A]] = {
    agent alter (oldState => {
      oldState match {
        case `zero` => buildQTree[A](xs)
        case _ => sg.sumOption(buildQTrees[A](xs) :+ oldState).get
      }
    })
  }
}
