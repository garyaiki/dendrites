/**
  */
package org

/** @author garystruthers
  *
  */
package object gs {
  def isElementEqual(p: Product, ele: Int, theType: Any): Boolean = {
    println(s"left:${p.productElement(ele)} right:$theType")
    p match {
      case p if(p.productArity >= ele && p.productElement(ele) == theType) => true
      case _ => false
    }
  }
}