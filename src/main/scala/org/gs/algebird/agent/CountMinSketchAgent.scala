package org.gs.algebird.agent

import akka.agent.Agent
import com.twitter.algebird._
import com.twitter.algebird.CMSHasherImplicits._
import org.gs.algebird._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.reflect.runtime.universe._

/** Shared state for CountMinSketch
  *
  * @see [[http://doc.akka.io/api/akka/current/#akka.agent.Agent]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.CMSHasher]
  * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering]]
  * @example [[org.gs.algebird.agent.CountMinSketchAgentSpec]]
  * @note I couldn't make this as generic because oldState both needs and rejects type parameters
  * but CMS turns every type into a long so the result is the same
  * 
  * @author garystruthers
  *
  */
class CountMinSketchAgent(val name: String = "")(implicit ec: ExecutionContext) {
  implicit val m = createCMSMonoid[Long]()
  val agent = Agent(m.zero)

  /** Update agent with sequence of Longs
    *
    * @param xs Seq
    * @return future of new value after this and all pending updates
    */
  def update(xs: Seq[Long]): Future[CMS[Long]] = {
    agent alter (oldState => {
      oldState ++ createCountMinSketch(xs)
    })
  }
}