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
package com.github.garyaiki.dendrites.fixtures

import scala.io.Source
import scala.language.postfixOps
import scala.sys.process._

/**
  *
  * @author Gary Struthers
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
    val fileLines = Source.fromFile(name).getLines.toSeq
    for (line <- fileLines) yield line.trim.toLowerCase
  }
}
