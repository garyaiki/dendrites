/**

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
package com.github.garyaiki.dendrites

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import java.util.concurrent.ThreadLocalRandom
import scala.concurrent.{Future, Promise}
import scala.concurrent.duration.FiniteDuration

/** Functions for concurrency
  *
  * Transform a Java Guava ListenableFuture into a Scala Future
  * {{{
  * val sessCloseF = cassandraSession.closeAsync()
  * val scalaSessF = listenableFutureToScala[Unit](sessCloseF.asInstanceOf[ListenableFuture[Unit]])
  * scalaSessF onComplete {
  *   case Success(x) => logger.debug("session closed")
  *   case Failure(t) => logger.error(t, "session closed failed {}", t.getMessage())
  * }
  * }}}
  *
  * Calculate exponential backoff delay
  * Constant params can be passed to first argument list on startup
  * {{{
  * val min = config getInt("dendrites.timer.min-backoff")
  * val minDuration = FiniteDuration(min, MILLISECONDS)
  * val max = config getInt("dendrites.timer.max-backoff")
  * val maxDuration = FiniteDuration(max, MILLISECONDS)
  * val randomFactor = config getDouble("dendrites.timer.randomFactor")
  * val curriedDelay = calculateDelay(minDuration, maxDuration, randomFactor) _
  * //Second arg list can be curried
  * val curriedDelay = consumerConfig.curriedDelay
  * val duration = curriedDelay(retries)
  * if(duration < maxDuration) {
  *   waitForTimer = true
  *   scheduleOnce(None, duration)
  * } else {
  *   failStage(e) // too many retries
  * }
  * }}}
  * @author Gary Struthers
  */
package object concurrent {

  /** Calls to Java libraries that return a Guava ListenableFuture can use this to transform it to a
    * Scala future
    *
    * @see [[https://github.com/google/guava/wiki/ListenableFutureExplained ListenableFutureExplained]]
    * @param lf ListenableFuture
    * @return completed Scala Future
    */
  def listenableFutureToScala[T](lf: ListenableFuture[T]): Future[T] = {
    val p = Promise[T]
    Futures.addCallback(lf, new FutureCallback[T] {
      def onFailure(t: Throwable): Unit = p failure t
      def onSuccess(result: T): Unit    = p success result
    })
    p.future
  }

  /** Calculate delay, used for exponential backoff. This is copied from Akka BackoffSupervisor
    * Used here in Akka Streams for the same purpose
    *
    * @param minBackoff minimum (initial) duration
    * @param maxBackoff the exponential back-off is capped to this duration
    * @param randomFactor after calculation of the exponential back-off an additional random delay
    * based on this factor is added, e.g. `0.2` adds up to `20%` delay.
    * In order to skip this additional delay pass in `0`.
    * @param retryCount in 2nd arg list for currying
    * @see [[https://github.com/akka/akka/blob/v2.4.9/akka-actor/src/main/scala/akka/pattern/BackoffSupervisor.scala BackoffSupervisor]]
    *
    */
  def calculateDelay(
    minBackoff: FiniteDuration,
    maxBackoff: FiniteDuration,
    randomFactor: Double)(retryCount: Int): FiniteDuration = {
      val rnd = 1.0 + ThreadLocalRandom.current.nextDouble * randomFactor
      if (retryCount >= 30) // Duration overflow protection
        maxBackoff
      else
        maxBackoff.min(minBackoff * math.pow(2, retryCount)) * rnd match {
          case f: FiniteDuration ⇒ f
            case _ ⇒ maxBackoff
        }
    }
}
