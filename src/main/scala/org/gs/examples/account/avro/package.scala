package org.gs.examples.account

import org.apache.avro.generic.GenericRecord

package object avro {

    def genericRecordToGetAccountBalances(record: GenericRecord): GetAccountBalances = {
      val obj = record.get("id")
      val l:Long = obj.asInstanceOf[Number].longValue
      GetAccountBalances(l)
  }
}