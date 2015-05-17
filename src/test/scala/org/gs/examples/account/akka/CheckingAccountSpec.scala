package org.gs.examples.account.akka

import akka.actor._
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.WordSpecLike
import org.scalatest.MustMatchers
import org.gs.testdriven.StopSystemAfterAll
import scala.collection.mutable.ArrayBuffer
import org.gs.examples.account.AccountType
import org.gs.examples.account.Checking
import akka.testkit.TestActorRef
import org.gs.examples.account.GetAccountBalances
import org.gs.examples.account.akka.AccountBalanceRetriever._

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
      val props = Props[CheckingAccountProxy]
      val proxy = system.actorOf(props, "Checking") 
      proxy ! GetAccountBalances(1L)
      expectMsg(CheckingAccountBalances(Some(List((3, 15000)))))

      //CheckingAccountProxy.fetchCheckingAccountsBalance(proxy, 1L, results, collectBalances)

    }

  }

}