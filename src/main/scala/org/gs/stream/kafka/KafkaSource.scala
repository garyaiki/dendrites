package org.gs.stream.kafka

import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.gs.kafka.ConsumerFacade

class KafkaSource[K, V](val consumer: ConsumerFacade[K, V])
    extends GraphStage[SourceShape[ConsumerRecords[K, V]]]{

  val kafkaConsumer = consumer.apply()
  val out = Outlet[consumer.OutType](s"KafkaSource")
  override val shape = SourceShape(out)
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      private var needCommit = false
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if(needCommit) kafkaConsumer.commitSync()
          val records = consumer.poll(kafkaConsumer)
          push(out, records)
          needCommit = true
        }
      })
    }
  }
}
