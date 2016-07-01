package org.gs.examples.account.actor

import akka.actor._
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{MustMatchers, WordSpecLike}
import scala.collection.mutable.ArrayBuffer
import org.gs.examples.account.{AccountType, Checking, CheckingAccountBalances, GetAccountBalances}
import org.gs.testdriven.StopSystemAfterAll

/**
  *
  * @author Gary Struthers
  */
class CheckingAccountSpec extends TestKit(ActorSystem("test"))
    with ImplicitSender
    with WordSpecLike
    with MustMatchers
    with StopSystemAfterAll {

  "A CheckingAccountProxy" must {
    "create" in {
      val results = ArrayBuffer.fill[Product](3)(None)
      val types: Set[AccountType] = Set(Checking)
      
      def collectBalances(force: Boolean = false) {
        val resultCount = results.count(_ != None)
        if ((resultCount == types.size) || force) {
          val result = results.toIndexedSeq
        }
      }
      
      val proxy = system.actorOf(CheckingAccountProxy.props, "Checking") 
      proxy ! GetAccountBalances(1L)

      expectMsg(CheckingAccountBalances[Int](Some(List((3, 15000)))))
    }
  }
}
