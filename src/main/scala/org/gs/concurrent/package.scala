package org.gs

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import scala.concurrent.{Future, Promise}

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
    val p = Promise[T]()
    Futures.addCallback(lf, new FutureCallback[T] {
      def onFailure(t: Throwable): Unit = p failure t
      def onSuccess(result: T): Unit    = p success result
    })
    p.future
  }
}
