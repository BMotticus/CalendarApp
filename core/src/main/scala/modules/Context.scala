package modules

import scala.concurrent.ExecutionContext

trait Context {
  def ec: ExecutionContext
}

trait ContextOps {
  protected def ctx: Context
  implicit def ec = ctx.ec
}
