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
package com.github.garyaiki.dendrites.avro4s

import com.sksamuel.avro4s.{FromRecord, SchemaFor, ToRecord}

trait Avro4sOps[A <: Product] {
  val schemaFor: AnyRef with SchemaFor[A]
  val toRecord: AnyRef with ToRecord[A]
  val fromRecord: AnyRef with FromRecord[A]
  def toBytes(caseClass: A): Array[Byte]
  def toCaseClass(bytes: Array[Byte]): A
}
