/**
 *
 */
package org.gs.akka.algebird.semigroup

import math.Ordered

import com.twitter.algebird._
import com.twitter.algebird.Operators._

import org.gs.akka.algebird.OrderedSemigroup._
/**
 * @author garystruthers
 *
 */
package object ordered {
  /*
  import scala.reflect.runtime.{ universe => ru }
  import scala.language.reflectiveCalls

  def getTypeTag[T: ru.TypeTag](obj: T) = ru.typeTag[T]
  val theType = getTypeTag(barackobama).tpe
  val decls = theType.decls.take(10)
  val m = ru.runtimeMirror(barackobama.getClass.getClassLoader)
  val compareTermSymb = ru.typeOf[TwitterUser].decl(ru.TermName("compare")).asMethod
  val im = m.reflect(barackobama)
  val compareMirror = im.reflectMethod(compareTermSymb)
  val comp = compareMirror(katyperry)
*/
  case class TwitterUser(val name: String, val numFollowers: Int) extends Ordered[TwitterUser] {
    def compare(that: TwitterUser): Int = {
      val c = this.numFollowers - that.numFollowers
      if (c == 0) this.name.compareTo(that.name) else c
    }
  }

  val barackobama = TwitterUser("BarackObama", 40267391)
  val katyperry = TwitterUser("katyperry", 48013573)
  val ladygaga = TwitterUser("ladygaga", 40756470)
  val miguno = TwitterUser("miguno", 731)
  val taylorswift = TwitterUser("taylorswift13", 37125055)
  val tws = List(barackobama,katyperry,ladygaga,miguno,taylorswift)

  val maxTU = tws.reduce[TwitterUser](maxResult[TwitterUser])
  val minTU = tws.reduce[TwitterUser](minResult[TwitterUser])
  val firstTU = tws.reduce[TwitterUser](firstResult[TwitterUser])
  val lastTU = tws.reduce[TwitterUser](lastResult[TwitterUser])




}