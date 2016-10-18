package org.gs.aggregator

object typetagsWS {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

  import scala.reflect.runtime.universe._
  val tt = typeTag[Int]                           //> tt  : reflect.runtime.universe.TypeTag[Int] = TypeTag[Int]
  sealed trait AccountType
  case object Checking extends AccountType
  case object Savings extends AccountType
  case object MoneyMarket extends AccountType
  val c = Checking                                //> c  : org.gs.aggregator.typetagsWS.Checking.type = Checking
  def paramInfo[T: TypeTag](x: T): Unit = {
    val targs = typeOf[T] match { case TypeRef(_, _, args) => args }
    println(s"type of $x has type arguments $targs")
  }                                               //> paramInfo: [T](x: T)(implicit evidence$2: reflect.runtime.universe.TypeTag[T
                                                  //| ])Unit
  def weakParamInfo[T](x: T)(implicit tag: WeakTypeTag[T]): Unit = {
    val targs = tag.tpe match { case TypeRef(_, _, args) => args }
    println(s"type of $x has type arguments $targs")
  }                                               //> weakParamInfo: [T](x: T)(implicit tag: reflect.runtime.universe.WeakTypeTag[
                                                  //| T])Unit
  paramInfo(41)                                   //> type of 41 has type arguments List()
  weakParamInfo(41)                               //> type of 41 has type arguments List()
  def foo[T] = weakParamInfo(List[T]())           //> foo: [T]=> Unit
  foo[Int]                                        //> type of List() has type arguments List(T)
  import scala.reflect._
  val ct = classTag[String]                       //> ct  : scala.reflect.ClassTag[String] = java.lang.String
  val cc = Checking.getClass                      //> cc  : Class[_ <: org.gs.aggregator.typetagsWS.Checking.type] = class org.gs.
                                                  //| aggregator.typetagsWS$$anonfun$main$1$Checking$2$
  val ccN = cc.getName                            //> ccN  : String = org.gs.aggregator.typetagsWS$$anonfun$main$1$Checking$2$
  val p = c.productPrefix                         //> p  : String = Checking
  val t2 = (Checking, 1)                          //> t2  : (org.gs.aggregator.typetagsWS.Checking.type, Int) = (Checking,1)
  val pt2 = t2.productPrefix                      //> pt2  : String = Tuple2
}