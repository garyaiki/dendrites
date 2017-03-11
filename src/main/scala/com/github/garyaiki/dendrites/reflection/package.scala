/**

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
package com.github.garyaiki.dendrites

import scala.reflect.runtime.universe.{Type, TypeRef, WeakTypeTag}

/** Reflection utils for logging and debugging
  *
  * Log type information
  * {{{
  * val result = results.toIndexedSeq
  * log.debug(s"result:${weakParamInfo(result)}")
  * }}}
  * @see [[http://docs.scala-lang.org/overviews/reflection/typetags-manifests.html TypeTags and Manifests]]
  * @author Gary Struthers
  */
package object reflection {

  /** Report type information, can be references to type parameters or abstract types. Use for
    * logging and debugging
    * to use, import scala.reflect.runtime.universe._
    * Warning Not thread safe, should be fine for logging and debugging though
    *
    * @tparam T type to get info for
    * @param tag implicit WeakTypeTag[T]
    * @return type information of element
    * @see [[http://docs.scala-lang.org/overviews/reflection/thread-safety.html reflection thread-safety]]
    * @see [[http://docs.scala-lang.org/overviews/reflection/overview.html reflection overview]]
    */
  def weakParamInfo[T](x: T)(implicit tag: WeakTypeTag[T]): List[Type] = tag.tpe match {
    case TypeRef(_, _, args) => args
  }
}
