/**
  */
package org.gs.fixtures

import scala.sys.process._
import scala.language.postfixOps

/** @author garystruthers
  *
  */
trait SysProcessUtils {

  def fileExists(name: String) = Seq("test", "-f", name).! == 0

  def properNamesPath(): String = {
    val shareProperNames = "/usr/share/dict/propernames" // OSX
    val properNames = "/usr/dict/propernames"

    if (fileExists(shareProperNames)) shareProperNames else properNames
  }

  def connectivesPath(): String = {
    val shareConnectives = "/usr/share/dict/connectives" // OSX
    val connectives = "/usr/dict/connectives"

    if (fileExists(shareConnectives)) shareConnectives else connectives
  }

  def wordsPath(): String = {
    val shareWords = "/usr/share/dict/words" // OSX
    val words = "/usr/dict/words"

    if (fileExists(shareWords)) shareWords else words
  }

  def wordCount(name: String): Int = {
    val line = Seq("wc", "-w", name).!!
    val reg = """\d+""".r
    (reg findFirstIn line get).toInt
  }

  def readWords(name: String): Seq[String] = {
    val fileLines = io.Source.fromFile(name).getLines.toSeq
    for (line <- fileLines) yield line.trim.toLowerCase
  }
}
object app extends SysProcessUtils {
  def main(args: Array[String]) = {
    val pNames = properNamesPath()
    val wc = wordCount(pNames)

    val words = readWords(pNames)
    val one = words.contains("gary")
    val two = words.contains("struthers")
    val three = words.contains("crispin")
    println(s"gary:$one struthers:$two crispin:$three")

    val NUM_HASHES = 3000
    val WIDTH = 32000
    val SEED = 10000
    import com.twitter.algebird._
    val bfMonoid = BloomFilter(wc, 0.01)
    val bf = bfMonoid.create(words: _*)
    val gary = (bf.contains("gary").isTrue)
    val struthers = (bf.contains("Struthers").isTrue)
    val crispin = (bf.contains("Crispin").isTrue)
    println(s"gary:$gary struthers:$struthers crispin:$crispin")

  }
}