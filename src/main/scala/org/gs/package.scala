
package org

import java.util.Properties

/** Utility functions for case classes and Properties files 
  *
  * @author Gary Struthers
  *
  * ==Load Properties file from classpath==
  * {{{
  *   val prop: Properties = loadProperties("kafkaProducer.properties")
  * }}}
  * 
  */
package object gs {

  /** Extract case class elements into a Map
    *
    * @example [[org.gs.http.caseClassToGetQuery]]
    *
    * @param cc case class (Product is supertype)
    * @return map of field names and values
    */
  def ccToMap(cc: Product) = cc.getClass.getDeclaredFields.foldLeft(Map[String, Any]()) {
    (a, f) =>
      f setAccessible(true) // to get private fields
      a + (f.getName -> f.get(cc))
  }

  /** Does the indexed case class field have desired type?
    *
    * @example [[org.gs.examples.account.actor.AccountBalanceRetrieverSpec]]
    *
    * @param case class (Product is supertype)
    * @param ele field element
    * @param theType type to match
    * @return true if element has theType
    */
  def isElementEqual(p: Product, ele: Int, theType: Any): Boolean = {
    p match {
      case _ if (p.productArity >= ele && p.productElement(ele) == theType) => true
      case _ => false
    }
  }

  /** Read Properties file
    *
    * @param filename must be in classpath, default src/main/resources
    * @return Properties object
    */
  def loadProperties(filename: String, path: String = "/"): Properties = {
    val prop = new Properties()
    prop load(getClass.getResourceAsStream(path + filename))
    prop
  }
}
