package org.gs.algebird.stream

import akka.stream.stage.{Context, PushStage, SyncDirective }
import com.github.nscala_time.time.StaticDateTime

class ZipTimeStage[A: Numeric] extends PushStage[Seq[A], Seq[(Double, Double)]] {
  val doubleTime = StaticDateTime.now.getMillis.toDouble
  def toZipTime(xs: Seq[A]): Seq[(Double, Double)] =
    xs.map(x => (x.asInstanceOf[Number].doubleValue(), doubleTime))
  override def onPush(elem: Seq[A], ctx: Context[Seq[(Double, Double)]]): SyncDirective =
          ctx.push(toZipTime(elem))
}