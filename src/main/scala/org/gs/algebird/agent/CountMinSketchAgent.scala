package org.gs.algebird.agent

import akka.agent.Agent
import com.twitter.algebird._
import com.twitter.algebird.CMSHasherImplicits._
import org.gs.algebird._
import org.gs.algebird.agent._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/** Shared state for CountMinSketch
  *
  * @see [[http://doc.akka.io/api/akka/current/#akka.agent.Agent]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.CMSHasher]
  * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering]]
  * @example [[org.gs.algebird.agent.CountMinSketchAgentSpec]]
  * 
  * @tparam K elements which are implicitly Ordering[K] and CMSHasher[K]
  * 
  * @author garystruthers
  *
  */
class CountMinSketchAgent[K: Ordering: CMSHasher](val name: String = "")
        (implicit ec: ExecutionContext) {
  implicit val m = createCMSMonoid[K]()
  val agent = Agent(m.zero)

  /** Update agent with sequence of Longs
    *
    * @param xs Seq
    * @return future of new value after this and all pending updates
    */
  def update(xs: Seq[K]): Future[CMS[K]] = {
    agent alter (oldState => {
      oldState ++ createCountMinSketch(xs)
    })
  }
}