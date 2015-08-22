/**
  */
package org

/** @author garystruthers
  *
  */
package object gs {

  /** Extract case class elements into a Map
    *  
    *  @param cc case class (Product is supertype) 
    *  @return map of field names and values
    */
  def ccToMap(cc: Product) = cc.getClass.getDeclaredFields.foldLeft(Map[String, Any]()) {
    (a, f) =>
      f.setAccessible(true) // to get private fields
      a + (f.getName -> f.get(cc))
  }
  
  /** Does the indexed case class field have desired type? 
    *  
    *  @param case class (Product is supertype)
    *  @param ele field element
    *  @param theType type to match
    *  @return true if element has theType
    */
  def isElementEqual(p: Product, ele: Int, theType: Any): Boolean = {
    p match {
      case p if (p.productArity >= ele && p.productElement(ele) == theType) => true
      case _ => false
    }
  }
}