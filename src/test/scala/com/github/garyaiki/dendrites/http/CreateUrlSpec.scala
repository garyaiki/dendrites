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
class CreateUrlSpec extends FlatSpec with Matchers {

  val scheme = "http"
  val ipDomain = "0.0.0.0"
  val port = 9000
  val path = "/account/balances/checking/"
  val testIpUrl = "http://0.0.0.0:9000/account/balances/checking/"
  val dnsDomain = "google.com"
  val testDnsUrl = "http://google.com:9000/account/balances/checking/"

  "createUrlSpec" should "create a valid url from components with an IP domain" in {
    val url = createUrl(scheme, ipDomain, port, path).toString
    assert(url === testIpUrl)
  }
  case class Mult(a: Char, b: Double)

  it should "create a valid url from components with an DNS domain" in {
    val url = createUrl(scheme, dnsDomain, port, path).toString
    assert(url === testDnsUrl)
  }
}
