package plugins

import com.typesafe.scalalogging.StrictLogging
import play.api.libs.concurrent.Execution
import play.api.{Application, Plugin}

trait BMPlugin extends Plugin with StrictLogging {
  implicit val app: Application

  implicit def ec = Execution.defaultContext
  implicit def bm: BMPlugin = this
  
  override def onStart () = {
    logger.info("BMPlugin started using " + Thread.currentThread.getName)
    super.onStart()
  }
  
  override def onStop () = {
    super.onStop()
  }
  
}
