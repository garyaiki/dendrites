package org.gs.aggregator.actor

import akka.actor.ActorRef

/** "expectOnce" result handler used by child actors that need an ActorRef that is not sender.
 *  Before creating actor curry originalSender then transform
 *  pfPlusSender from a regular function to a PartialFunction
 *  {{{
 *  def f = pfPlusSender(originalSender)(_)
 *  val pf = PartialFunction(f)
 *  }}}
 *
 * @author garystruthers
 *
 */
trait PartialFunctionPlusSender {
  def pfPlusSender(originalSender: ActorRef)(a: Any): Unit
}
