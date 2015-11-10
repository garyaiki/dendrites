package org.gs.stream

import akka.stream.stage.{Context, PushStage, SyncDirective }

class MapPushStage[A, B](f: A => B) extends PushStage[A, B] {
  override def onPush(elem: A, ctx: Context[B]): SyncDirective =
    ctx.push(f(elem))
}