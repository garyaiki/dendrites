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

