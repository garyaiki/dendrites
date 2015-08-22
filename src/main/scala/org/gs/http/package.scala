/**
  */
package org.gs

import org.gs._

/** @author garystruthers
  *
  */
package object http {

  /** Extract case class elements into http GET query
    *  
    *  @param cc case class (Product is supertype) 
    *  @return field names and values as GET query string preceded with ccName? 
    */
  def caseClassToGetQuery(cc: Product): String = {
    val sb = new StringBuilder(cc.productPrefix)
    sb.append('?')
    val fields = ccToMap(cc).filterKeys(_ != "$outer")
    fields.foreach{
      case (key, value) => sb.append(key).append('=').append(value).append('&')
    }
    if(sb.last == '&') sb.setLength(sb.length() - 1)
    sb.toString
  }

}