package com.github.garyaiki.dendrites.avro4s

import com.sksamuel.avro4s.{FromRecord, SchemaFor, ToRecord}

trait Ops[A <: Product] {
  val schemaFor: AnyRef with SchemaFor[A]
  val toRecord: AnyRef with ToRecord[A]
  val fromRecord: AnyRef with FromRecord[A]
  def toBytes(caseClass: A): Array[Byte]
  def toCaseClass(bytes: Array[Byte]): A
}