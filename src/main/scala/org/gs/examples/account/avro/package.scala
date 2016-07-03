package org.gs.examples.account

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

    def genericRecordToGetAccountBalances(record: GenericRecord): GetAccountBalances = {
      val obj = record.get("id")
      val l:Long = obj.asInstanceOf[Number].longValue
      GetAccountBalances(l)
  }
}
