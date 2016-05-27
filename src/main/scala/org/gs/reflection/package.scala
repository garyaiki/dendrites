
package org.gs

import scala.reflect.runtime.universe.{Type, TypeRef, WeakTypeTag}

/** reflection aids for logging and debugging
  *
  * @author Gary Struthers
  */
package object reflection {

  /** Report type information, uses scala's enhanced reflection, useful for logging and debugging
    *
    * to use, import scala.reflect.runtime.universe._
    * Warning Not thread safe, should be fine for logging and debugging though
    * @see [[http://docs.scala-lang.org/overviews/reflection/thread-safety.html "reflection thread-safety"]]
    * @see [[http://docs.scala-lang.org/overviews/reflection/overview.html "reflection overview"]]
    *
    * @param implicit WeakTypeTag
    * @return type information of element
    */
  def weakParamInfo[T](x: T)(implicit tag: WeakTypeTag[T]): List[Type] =
    tag.tpe match { case TypeRef(_, _, args) => args }
}
