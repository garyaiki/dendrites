package com.github.garyaiki.dendrites.avro
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}

trait Ops[A <: Product] {
  def schemaFor(fileName: Option[String]): Schema
  def toRecord(gRecord: GenericData.Record)(caseClass: A): Unit
  def toBytes(schema: Schema)(caseClass: A): Array[Byte]
  def fromRecord(rec: GenericRecord): A
}