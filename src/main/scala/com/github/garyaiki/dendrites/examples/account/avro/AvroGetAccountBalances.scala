package com.github.garyaiki.dendrites.examples.account.avro

import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericDatumWriter, GenericRecord}
import com.github.garyaiki.dendrites.ccToMap
import com.github.garyaiki.dendrites.avro.Ops
import com.github.garyaiki.dendrites.avro.{ccToGenericRecord, loadSchema, toByteArray}
import com.github.garyaiki.dendrites.examples.account.{AccountType, GetAccountBalances}

object AvroGetAccountBalances extends Ops[GetAccountBalances] {

  def schemaFor(fileName: Option[String]): Schema = loadSchema("getAccountBalances.avsc")

  def toRecord(gRecord: GenericData.Record)(caseClass: GetAccountBalances): Unit = {
    ccToGenericRecord(gRecord)(caseClass)
  }

  def toBytes(schema: Schema)(caseClass: GetAccountBalances): Array[Byte] = {
    val record = new GenericData.Record(schema)
    val writer = new GenericDatumWriter[GenericRecord](schema)
    toRecord(record)(caseClass)
    toByteArray(writer)(record)
  }

    /** Avro GenericRecord is mapped to a Scala class by getting a record field by name then passing it to
    * the Scala classes' constructor
    *
    * @param record Avro GenericRecord
    * @return Scala class with GenericRecord values
    */
  def fromRecord(record: GenericRecord): GetAccountBalances = {
    val obj = record.get("id")
    val l: Long = obj.asInstanceOf[Number].longValue
    GetAccountBalances(l)
  }
}