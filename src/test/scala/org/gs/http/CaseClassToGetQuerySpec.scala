/**
  */
package org.gs.http

import org.scalatest.{ FlatSpec, Matchers }

/** @author garystruthers
  *
  */
class CaseClassToGetQuerySpec extends FlatSpec with Matchers {
  case class Sing(s: String)

  "caseClassToGetQuery" should "create a 1 arg GET query string" in {
    val sing = Sing("value")
    val q = caseClassToGetQuery(sing)
    assert(q === "Sing?s=value")
  }
  case class Mult(a: Char, b: Double)

  it should "create a 2 arg GET query string" in {
    val mult = Mult('A', 10.0)
    val q = caseClassToGetQuery(mult)
    assert(q === "Mult?a=A&b=10.0")
  }
}