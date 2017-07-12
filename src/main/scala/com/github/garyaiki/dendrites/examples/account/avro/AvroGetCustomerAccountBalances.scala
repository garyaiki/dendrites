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

import java.util.{List => JList}
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericDatumWriter, GenericRecord}
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.{SpecificDatumReader, SpecificDatumWriter}
import org.apache.avro.util.Utf8
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import com.github.garyaiki.dendrites.ccToMap
import com.github.garyaiki.dendrites.avro.AvroOps
import com.github.garyaiki.dendrites.avro.{loadSchema, toByteArray}
import com.github.garyaiki.dendrites.examples.account.{AccountType, Checking, MoneyMarket, GetCustomerAccountBalances,
  Savings}

object AvroGetCustomerAccountBalances extends AvroOps[GetCustomerAccountBalances] {

  def schemaFor(path: Option[String], fileName: String): Schema = path match {
    case Some(p) => loadSchema(fileName, p)
    case None => loadSchema(fileName)
    case _ => loadSchema("getCustomerStringAccountBalances.avsc", "/avro/")
  }

  val schema: Schema = schemaFor(Some("/avro/"), "getCustomerStringAccountBalances.avsc")

  def toStrings(from: Set[AccountType]): JList[String] = {
    val ab = new ArrayBuffer[String]
    from foreach (x => ab += x.productPrefix)
    ab.asJava
  }

  /** Map case class values that are only simple types to GenericRecord
    *
    * @tparam A case class or tuple
    * @param cc a case class (or tuple)
    * @return GenericData.Record initialized with schema for case class
    */
  def toRecord(schema: Schema, caseClass: GetCustomerAccountBalances): GenericData.Record = {
    val kvMap = ccToMap(caseClass)
    val record = new GenericData.Record(schema)
    kvMap foreach {
      case (key, value) if(key == "id") => record.put(key, value)
      case (key, value) if(key == "accountTypes") => record.put(key, toStrings(value.asInstanceOf[Set[AccountType]]))
    }
    record
  }

  def toBytes(schema: Schema, caseClass: GetCustomerAccountBalances): Array[Byte] = {
    val writer = new GenericDatumWriter[GenericRecord](schema)
    val record = toRecord(schema, caseClass)
    toByteArray(writer)(record)
  }

  def fromStrings(from: Seq[Utf8]): Set[AccountType]= {
    val ab = new ArrayBuffer[AccountType]
    from foreach {x => x.toString match {
      case "Checking" => ab += Checking
      case "MoneyMarket" => ab += MoneyMarket
      case "Savings" => ab += Savings
    }}
    ab.toSet
  }

  def fromRecord(record: GenericRecord): GetCustomerAccountBalances = {
    val idObj = record.get("id")
    val l: Long = idObj.asInstanceOf[Number].longValue
    val accountTypesObj = record.get("accountTypes")
    val at = accountTypesObj.asInstanceOf[GenericData.Array[Utf8]]
    val set = fromStrings(at.asScala.toSeq)
    GetCustomerAccountBalances(l, set)
  }
}
