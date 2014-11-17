/**
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *
  */
object FUnctor {
  //type class example
  object Math {
    import annotation.implicitNotFound
    @implicitNotFound(msg = "Cannot find NumberLike type class for ${T}")
    trait NumberLike[T] {
      def plus(x: T, y: T): T
      def divide(x: T, y: Int): T
      def minus(x: T, y: T): T
    }
    object NumberLike {
      implicit object NumberLikeDouble extends NumberLike[Double] {
        def plus(x: Double, y: Double): Double = x + y
        def divide(x: Double, y: Int): Double = x / y
        def minus(x: Double, y: Double): Double = x - y
      }
      implicit object NumberLikeInt extends NumberLike[Int] {
        def plus(x: Int, y: Int): Int = x + y
        def divide(x: Int, y: Int): Int = x / y
        def minus(x: Int, y: Int): Int = x - y
      }
    }
  }
  import Math.NumberLike
  def mean[T](xs: Vector[T])(implicit ev: NumberLike[T]): T =
    ev.divide(xs.reduce(ev.plus(_, _)), xs.size)
  def median[T: NumberLike](xs: Vector[T]): T = xs(xs.size / 2)
  def quartiles[T: NumberLike](xs: Vector[T]): (T, T, T) =
    (xs(xs.size / 4), median(xs), xs(xs.size / 4 * 3))
  def iqr[T: NumberLike](xs: Vector[T]): T = quartiles(xs) match {
    case (lowerQuartile, _, upperQuartile) =>
      implicitly[NumberLike[T]].minus(upperQuartile, lowerQuartile)
  }

  val xs = Vector(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)
  val mxs = mean(xs)
  val xsi = Vector(1, 2, 3, 4, 5, 6, 7, 8, 9)
  val mxsi = mean(xsi)
  val mxsim = median(xsi)
  val mxsiq = quartiles(xsi)
  val mxsiqr = iqr(xsi)

  import java.util.{ List => JList }

  case class Box2[A](a1: A, a2: A)
  implicit object Box2Functor extends Functor[Box2] {
    def map[A, B](b: Box2[A])(f: A => B): Box2[B] = Box2(f(b.a1), f(b.a2))
  }
  import scala.language.higherKinds
  def describe[A, F[_]: Functor](fa: F[A]) = implicitly[Functor[F]].map(fa)(a => a.toString())

  case class Holder(i: Int)
  val jlist: JList[Holder] = {
    val l = new java.util.ArrayList[Holder]()
    l add Holder(1); l add Holder(2); l add Holder(3)
    l
  }
  implicit object JavaListFunctor extends Functor[JList] {
    import collection.JavaConverters._
    def map[A, B](fa: JList[A])(f: A => B): JList[B] = (for (a <- fa.asScala) yield f(a)).asJava
  }
  val list = describe(jlist)
  val box2 = describe(Box2(Holder(4), Holder(5)))
  implicit object ListFunctor extends Functor[List] {
    def map[A, B](fa: List[A])(f: A => B): List[B] = (for (a <- fa) yield f(a))
  }
  implicit object OptionFunctor extends Functor[Option] {
    def map[A, B](fa: Option[A])(f: A => B): Option[B] = (for (a <- fa) yield f(a))
  }
  implicit object VectorFunctor extends Functor[Vector] {
    def map[A, B](fa: Vector[A])(f: A => B): Vector[B] = (for (a <- fa) yield f(a))
  }

}