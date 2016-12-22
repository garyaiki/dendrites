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
package com.github.garyaiki.dendrites.examples.account

import org.apache.avro.generic.GenericRecord

/** Avro functions for account examples
  *
  * Map Avro GenericRecord to GetAccountBalances
  * {{{
  * val deserializer = new AvroDeserializer("getAccountBalances.avsc",
  *         genericRecordToGetAccountBalances)
  * }}}
  */
package object avro {

  /** Avro GenericRecord is mapped to a Scala class by getting a record field by name then passing it to
    * the Scala classes' constructor
   	*
   	* @param record Avro GenericRecord
    * @return Scala class with GenericRecord values
    */
  def genericRecordToGetAccountBalances(record: GenericRecord): GetAccountBalances = {
      val obj = record.get("id")
      val l:Long = obj.asInstanceOf[Number].longValue
      GetAccountBalances(l)
  }
}
