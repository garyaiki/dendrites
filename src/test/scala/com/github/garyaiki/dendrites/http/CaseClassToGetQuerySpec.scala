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
package com.github.garyaiki.dendrites.http

import org.scalatest.{FlatSpec, Matchers}

/**
  *
  * @author Gary Struthers
  */
class CaseClassToGetQuerySpec extends FlatSpec with Matchers {

  case class Sing(s: String)

  "caseClassToGetQuery" should "create a 1 arg GET query string" in {
    val sing = Sing("value")
    val q = caseClassToGetQuery(sing, sing.productPrefix).toString
    assert(q === "Sing?s=value")
  }
  case class Mult(a: Char, b: Double)

  it should "create a 2 arg GET query string" in {
    val mult = Mult('A', 10.0)
    val q = caseClassToGetQuery(mult, mult.productPrefix).toString
    assert(q === "Mult?a=A&b=10.0")
  }
}
