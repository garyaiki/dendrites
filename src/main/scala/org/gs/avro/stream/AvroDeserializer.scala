package org.gs.avro.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import org.apache.avro.generic.GenericRecord
import org.apache.avro.Schema
import org.gs.avro.{byteArrayToGenericRecord, loadSchema}

/** Maps a byteArray first to an Avro GenericRecord, then maps the GenericRecord to a case class
  *
  * @tparam A case class or tuple subclass of Product
  * @param filename of Avro schema, must be in classpath
  * @param f user function copies values from Avro GenericRecord to case class
  * @author Gary Struthers
  */
class AvroDeserializer[A <: Product](filename: String, f:(GenericRecord) => A)
    extends GraphStage[FlowShape[Array[Byte], A]] {

  val in = Inlet[Array[Byte]]("GenericSerializer.in")
  val out = Outlet[A]("GenericSerializer.out")

  override val shape = FlowShape.of(in, out)
  val schema = loadSchema(filename)

  /** Deserialize bytearray to Avro GenericRecord on push
    *
    * @param inheritedAttributes
    * @return GenericRecord
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val bytes = grab(in)
          val record = byteArrayToGenericRecord(schema, bytes)
          push(out, f(record))
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
}
