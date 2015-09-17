/**
  */
package org.gs.http

import org.scalatest.{ FlatSpec, Matchers }

/** @author garystruthers
  *
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