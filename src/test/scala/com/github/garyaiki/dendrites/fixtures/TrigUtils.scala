/**
  */
package com.github.garyaiki.dendrites.fixtures

/** @author garystruthers
  *
  */
trait TrigUtils {

  def genSineWave(amplitude: Int, degrees: Range): Seq[Double] = {

    for(i <- degrees) yield {
      val s = amplitude * math.sin(math.toRadians(i))
      BigDecimal(s).setScale(4, BigDecimal.RoundingMode.HALF_UP).toDouble
    }
  }
}

object TestTrigUtils extends TrigUtils{
  def main(args: Array[String]): Unit = {
    val sines = genSineWave(100, 0 to 360)
    println(s"sine 0: ${sines(0)}")
    println(s"sine 90: ${sines(90)}")
    println(s"sine 180: ${sines(180)}")
    println(s"sine 270: ${sines(270)}")
    println(s"sine 360: ${sines(360)}")
  }
}
