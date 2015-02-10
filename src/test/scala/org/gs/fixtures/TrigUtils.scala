/**
  */
package org.gs.fixtures

/** @author garystruthers
  *
  */
trait TrigUtils {
  
  def sinePeriod(amplitude: Int): Seq[Double] = {
    val r = 0 to 360
    for(i <- r) yield {
      val s = amplitude * math.sin(math.toRadians(i))
      BigDecimal(s).setScale(4, BigDecimal.RoundingMode.HALF_UP).toDouble
    }
  }

}

object TestTrigUtils extends TrigUtils{
  def main(args: Array[String]): Unit = {
    val sines = sinePeriod(100)
    println(s"sine 0: ${sines(0)}")
    println(s"sine 90: ${sines(90)}")
    println(s"sine 180: ${sines(180)}")
    println(s"sine 270: ${sines(270)}")
    println(s"sine 360: ${sines(360)}")
  }
}