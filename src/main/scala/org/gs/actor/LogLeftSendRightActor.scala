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
package org.gs.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Stash, Terminated}
import org.gs.stream.actor.OtherActor
import LogLeftSendRightActor.ResultsActor

/** Actor that logs Either Left errors and sends on Either Right results.
  *
  * Watches Results actor and changes state to waiting when it dies
  * @see [[http://doc.akka.io/api/akka/current/#akka.actor.Stash Stash]]
  * @param initActor ActorRef and name of actor to send results to.
  * @author Gary Struthers
  */
class LogLeftSendRightActor(initActor: ResultsActor) extends Actor with Stash with ActorLogging {

  implicit val system = context.system
  implicit val logger = log
  var resultsActor: ResultsActor = initActor

  override def preStart() = {
    context.watch(resultsActor.ref)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    context.unwatch(resultsActor.ref)
    super.preRestart(reason, message)
    log.error(reason, "Restarting due to [{}] when processing [{}]",
        reason.getMessage, message.getOrElse(""))
  }

  def ready: Receive = {
    case x: (Seq[String], Seq[AnyRef]) ⇒ {
      for(errorMsg <- x._1) {System.out.println(s"errorMsg:$errorMsg")
        log.warning(errorMsg)
      }/*
      for(results <- x._2) {System.out.println(s"results:$results")
        resultsActor.ref ! results
      }*/
      resultsActor.ref ! x._2
      //resultsActor.ref ! "complete"
    }

    case Terminated(actor) ⇒ {
      context.parent ! ResultsActor(null, resultsActor.name)
      context.unwatch(resultsActor.ref)
      context.become(waiting)
      log.warning("resultsActor {} terminated", actor)
    }
  }

  def waiting: Receive = {
    case x: (Seq[String], Seq[AnyRef]) ⇒ stash()

    case Terminated(actor) ⇒ {
      context.parent ! ResultsActor(null, resultsActor.name)
      context.unwatch(resultsActor.ref)
      log.warning("resultsActor {} terminated while not ready", actor)
    }

    case newResults: ResultsActor => {
      resultsActor = newResults
      context.watch(resultsActor.ref)
      unstashAll()
      context.become(ready)
    }    
  }

  def receive = ready
}

object LogLeftSendRightActor {
  case class ResultsActor(override val ref: ActorRef, override val name: String) extends OtherActor

  def props(resultsActor: ResultsActor): Props = Props(classOf[LogLeftSendRightActor], resultsActor)
}
