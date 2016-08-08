package modules

import scala.concurrent.ExecutionContext

trait Context {
  def ec: ExecutionContext
}
