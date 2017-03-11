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
package com.github.garyaiki.dendrites.examples.account.avro

import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericDatumWriter, GenericRecord}
import com.github.garyaiki.dendrites.ccToMap
import com.github.garyaiki.dendrites.avro.AvroOps
import com.github.garyaiki.dendrites.avro.{ccToGenericRecord, loadSchema, toByteArray}
import com.github.garyaiki.dendrites.examples.account.{AccountType, GetAccountBalances}

object AvroGetAccountBalances extends AvroOps[GetAccountBalances] {

  def schemaFor(path: Option[String], fileName: String): Schema = path match {
    case Some(p) => loadSchema(fileName, p)
    case None    => loadSchema(fileName)
    case _       => loadSchema("getAccountBalances.avsc", "/avro/")
  }

  def toRecord(schema: Schema, caseClass: GetAccountBalances): GenericData.Record = {
    val record = new GenericData.Record(schema)
    ccToGenericRecord(record)(caseClass)
    record
  }

  def toBytes(schema: Schema, caseClass: GetAccountBalances): Array[Byte] = {
    val writer = new GenericDatumWriter[GenericRecord](schema)
    val record = toRecord(schema, caseClass)
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
