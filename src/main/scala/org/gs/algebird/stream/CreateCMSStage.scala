package org.gs.algebird.stream

import akka.stream.stage.{Context, PushStage, SyncDirective }
import com.twitter.algebird.{CMS, CMSHasher, CMSMonoid}

import org.gs.algebird.{createCMSMonoid, createCountMinSketch}

class CreateCMSStage[K: Ordering: CMSHasher] extends PushStage[Seq[K], CMS[K]] {
  implicit val monoid: CMSMonoid[K] = createCMSMonoid[K]()
  override def onPush(elem: Seq[K], ctx: Context[CMS[K]]): SyncDirective =
          ctx.push(createCountMinSketch(elem))
}
