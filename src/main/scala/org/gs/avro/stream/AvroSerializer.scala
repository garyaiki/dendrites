package org.gs.avro.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import org.apache.avro.Schema
import org.gs.avro._

/** Flow serializes a case class. Read Avro schema, pass it and a case class to f, push serialized
  * object 
  * 
  * @author Gary Struthers
  * @tparam A case classes and tuples are subtypes of Produce
  * @tparam B type of serialized object, i.e. Array[Byte]
  * @param filename of Avro schema for case class, must be on classpath
  * @param serialization function
  */
class AvroSerializer[A <: Product, B](filename: String,
    f:(Schema, A) => B)
    extends GraphStage[FlowShape[A, B]] {
  
  val in = Inlet[A]("GenericSerializer.in")
  val out = Outlet[B]("GenericSerializer.out")

  override val shape = FlowShape.of(in, out)
  val schema = loadSchema(filename)
  override def createLogic(attr: Attributes): GraphStageLogic = 
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          push(out, f(schema, grab(in)))
        }
      })
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
}