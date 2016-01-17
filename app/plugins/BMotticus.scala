package plugins

import com.edulify.play.hikaricp._
import play.api.Play.current
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.concurrent.{Akka, Execution}
import play.api.Application
import play.api.db._
import modules._

class BMotticus (implicit val app: Application) extends BMPlugin with Context with StrictLogging{
  
  lazy val config = app.configuration

  implicit def ctx: modules.Context = this
  
  implicit def actorSystem = Akka.system
  
  lazy val usersM = new modules.UsersModule(ctx)
  
  override def onStart() = {
    logger.info("BMotticus application started using " + Thread.currentThread.getName)
    super.onStart()
  }
  
  override def onStop () = {
    logger.info("Application Shutting down")
    super.onStop()
  }
  
}

trait BMotticusContext {
  def bm: plugins.BMotticus = current.plugin[BMotticus].getOrElse(throw new RuntimeException("Plugin Failed to load"))
}

object BMotticusContext extends BMotticusContext
