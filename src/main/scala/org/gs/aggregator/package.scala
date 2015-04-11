/**
  */
package org.gs

/** @author garystruthers
  *
  */
import scala.annotation.tailrec
package object aggregator {
  import shapeless._
  import syntax._
  import zipper._

  case class Aggregate[E <: HList](val hl: E = HNil) {

    def update(x: Product) = {
      val hl2 = hl.toZipper.insert(x).reify
      Aggregate[HList](hl2)
    }

    def hasAll(total: Int): Boolean = {
      val count = new HListOps(hl).runtimeLength
      if (count < total) false else true
    }

    def apply(n: Int): Product = {
      @tailrec
      def loop(l: HList, acc: Int): Product = l match {
        case hd :: tl if (acc == n) => hd match { case a: Product => a }
        case hd :: tl               => loop(tl, acc + 1)
        case _ => None
      }

      loop(hl, 0)
    }

    def select(x: Product): Product = {
      @tailrec
      def loop(l: HList): Product = l match {
        case hd :: tl if(hd.getClass == x.getClass) => hd match { case a: Product => a }
        case hd :: tl => loop(tl)
        case _ => None
      }

      loop(hl)
    }
  }
}