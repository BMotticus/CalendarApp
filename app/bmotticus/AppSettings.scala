package bmotticus

import com.typesafe.scalalogging.StrictLogging
import play.api.{Application, Plugin}
import play.api.libs.concurrent.Execution
import com.edulify.play.hikaricp._

class BMPlugin(implicit val app: Application) extends Plugin with StrictLogging{
  lazy val config = app.configuration
  
  implicit def ec = Execution.defaultContext
  
  override def onStart() = {
    super.onStart()
  }

  override def onStop() = {
    super.onStop()
  }

  override def enabled = true
}


