package org.gs.examples.account.http

import akka.stream.scaladsl.Flow
import scala.collection.mutable.ArrayBuffer

package object stream {
  
  def tuple3LeftRight(in: (Either[String, AnyRef], Either[String, AnyRef], Either[String, AnyRef])):
          (Seq[String], Seq[AnyRef]) = {

    val lefts = new ArrayBuffer[String]()
    val rights = new ArrayBuffer[AnyRef]()
    in._1 match {
      case Left(l)  => lefts.append(l)
      case Right(r) => rights.append(r)
    }
    in._2 match {
      case Left(l)  => lefts.append(l)
      case Right(r) => rights.append(r)
    }
    in._3 match {
      case Left(l)  => lefts.append(l)
      case Right(r) => rights.append(r)
    }
    (lefts.toSeq, rights.toSeq)
  }
 
  def leftRightFlow: Flow[(Either[String, AnyRef], Either[String, AnyRef], Either[String, AnyRef]),
          (Seq[String], Seq[AnyRef]), Unit] =
    Flow[(Either[String, AnyRef], Either[String, AnyRef], Either[String, AnyRef])].
    map(tuple3LeftRight)  
}