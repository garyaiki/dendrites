package org.gs.algebird.stream

import akka.stream.stage.{Context, PushStage, SyncDirective }
import com.twitter.algebird.{HLL, HyperLogLogAggregator}

import org.gs.algebird.createHLL
import org.gs.algebird.AlgebirdConfigurer.hyperLogLogBits
import org.gs.algebird.typeclasses.HyperLogLogLike

class CreateHLLStage[A: HyperLogLogLike](bits: Int = hyperLogLogBits)
  extends PushStage[Seq[A], HLL] {

  implicit val ag = HyperLogLogAggregator(bits)

  override def onPush(elem: Seq[A], ctx: Context[HLL]): SyncDirective = ctx.push(createHLL(elem))
}
