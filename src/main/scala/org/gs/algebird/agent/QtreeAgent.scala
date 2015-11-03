package org.gs.algebird.agent

import akka.agent.Agent
import com.twitter.algebird._
import org.gs.algebird._
import org.gs.algebird.typeclasses.QTreeLike
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.reflect.runtime.universe._

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
class QTreeAgent[A: QTreeLike : TypeTag](val name: String = "",
                                         level: Int = 16,
                                         xs: Option[Seq[A]] = None)
             (implicit ec: ExecutionContext, sg: QTreeSemigroup[A]) {
  val zero = sg.underlyingMonoid.zero
  val agent = xs match {
    case None    => Agent(buildQTree[A](Seq[A](zero)))
    case Some(xs) => Agent(buildQTree[A](xs))
  }

  /** Update agent with sequence of values
    *
    * @param xs Seq of BigDecimal, BigInt, Double, Float, Int or Long
    * @return future of new QTree after this and all pending updates
    */
  def update(xs: Seq[A]): Future[QTree[A]] = {
    agent alter (oldState => {
      oldState match {
        case zero => buildQTree[A](xs)
        case _ => sg.plus(oldState, buildQTree[A](xs))
      }
    })
  }
}
