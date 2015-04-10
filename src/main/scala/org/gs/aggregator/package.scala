/**
  */
package org.gs

/** @author garystruthers
  *
  */
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
  }
}