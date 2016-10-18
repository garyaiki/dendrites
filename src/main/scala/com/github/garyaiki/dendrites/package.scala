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
package org

import java.util.Properties

/** Utility functions for case classes and Properties files
  *
  * == ccToMap ==
  *  Case class fields to map of field names and values
  * {{{
  * val kvMap = ccToMap(cc)
  *  kvMap foreach {
  *   case (key, value) => gRecord.put(key, value)
  * }
  * }}}
  *
  * == isElementEqual ==
  *  Is case class field at index a specified type
  * {{{
  * system.actorOf(props) ! GetCustomerAccountBalances(2, Set(Checking, Savings, MoneyMarket))
  *   receiveOne(2.seconds) match {
  *    case result: IndexedSeq[Product] ⇒ {
  *     assert(isElementEqual(result(0), 0, Checking))
  *     assert(isElementEqual(result(1), 0, Savings))
  *     assert(isElementEqual(result(2), 0, MoneyMarket))
  *    }
  *    case result ⇒ assert(false, s"Expect 3 AccountTypes, got $result")
  *   }
  * }}}
  *
  * == loadProperties ==
  * Load Properties file from classpath
  * {{{
  *   val prop: Properties = loadProperties("kafkaProducer.properties")
  * }}}
  * @author Gary Struthers
  */
package object gs {

  /** Extract case class elements into a Map
    *
    * @param cc case class (Product is super type)
    * @return map of field names and values
    */
  def ccToMap(cc: Product): Map[String, Any] =
            cc.getClass.getDeclaredFields.foldLeft(Map[String, Any]()) {
    (a, f) =>
      f setAccessible(true) // to get product's private fields
      a + (f.getName -> f.get(cc))
  }

  /** Does the indexed case class field have desired type?
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
    * @param filename, must be in classpath,
    * @param path, default src/main/resources
    * @return Properties object
    */
  def loadProperties(filename: String, path: String = "/"): Properties = {
    val prop = new Properties()
    prop load(getClass.getResourceAsStream(path + filename))
    prop
  }
}
