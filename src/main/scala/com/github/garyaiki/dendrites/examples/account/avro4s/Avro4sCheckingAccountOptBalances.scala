package com.github.garyaiki.dendrites.examples.account.avro4s

import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream, AvroSchema, FromRecord, SchemaFor, ToSchema, ToRecord}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import org.apache.avro.Schema
import com.github.garyaiki.dendrites.avro4s.Avro4sOps

/** CheckingAccountOptBalances serialize/deserialize with Avro4s.
  *
  *
  * @author Gary Struthers
  */
object Avro4sCheckingAccountOptBalances  extends Avro4sOps[CheckingAccountOptBalances] {
  implicit val schemaFor = SchemaFor[CheckingAccountOptBalances]
  implicit val toRecord = ToRecord[CheckingAccountOptBalances]
  implicit val fromRecord = FromRecord[CheckingAccountOptBalances]  // Scala-ide shows error, No error in sbt, maven
  val schema = AvroSchema[CheckingAccountOptBalances]

  /** CheckingAccountOptBalances to Array[Byte]
    *
    * @param caseClass
    * @return Array[Byte]
    */
  def toBytes(caseClass: CheckingAccountOptBalances): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[CheckingAccountOptBalances](baos)
    output.write(caseClass)
    output.close()
    baos.toByteArray
  }

  /** Array[Byte] to CheckingAccountOptBalances
    *
    * @param bytes
    * @return CheckingAccountOptBalances
    */
  def toCaseClass(bytes: Array[Byte]): CheckingAccountOptBalances = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[CheckingAccountOptBalances](in)
    val result = input.iterator.toSeq
    result(0)
  }
}
