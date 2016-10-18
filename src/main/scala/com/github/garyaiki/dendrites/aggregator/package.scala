/** Copyright 2016 Gary Struthers

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
package com.github.garyaiki.dendrites

/** Aggregate functions
  *
  * == mean ==
  * Mean of sequence of Numeric
  * {{{
  * val doubles = List(1.0, 2.1, 3.2)
  * val m: Either[String,Double] = mean(doubles)
  * }}}
  * @author Gary Struthers
  */
package object aggregator {

  /** Find arithmetic mean of a Sequence of Numeric elements
    *
    * @tparam A Numeric type
    * @param xs
    * @return mean of xs in Right or error message in Left
    */
  def mean[A: Numeric](xs: Seq[A]): Either[String, A] = implicitly[Numeric[A]] match {
    case num: Fractional[_] =>
      import num._;
      Right(xs.sum / fromInt(xs.size))
    case num: Integral[_] =>
      import num._;
      Right(xs.sum / fromInt(xs.size))
    case num: Numeric[_] => Left(s"$num is not divisable")
  }
}
