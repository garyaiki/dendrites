package com.github.garyaiki.dendrites.avro
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}

trait AvroOps[A <: Product] {
  def schemaFor(path: Option[String], fileName: String): Schema
  def toRecord(schema: Schema, caseClass: A): GenericData.Record
  def toBytes(schema: Schema, caseClass: A): Array[Byte]
  def fromRecord(rec: GenericRecord): A
}