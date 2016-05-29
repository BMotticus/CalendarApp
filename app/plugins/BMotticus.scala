package plugins

import com.typesafe.scalalogging.StrictLogging
import play.api.libs.concurrent.{Akka, Execution}
import play.api.Application
import modules._
import play.api.libs.ws._
import javax.inject.{Inject,Singleton}

class BMotticus  @Inject() (implicit val app: Application) extends BMPlugin with Context with StrictLogging{
  
  lazy val config = app.configuration

  implicit def ctx: modules.Context = this
  
  implicit def actorSystem = Akka.system
  
  lazy val oAuthConfigs = new OAuthConfiguration(config.getObject("google.client_id.json.web").get)
  
  lazy val usersM = new modules.UsersModule(ctx)
  lazy val accountsM = new modules.AccountsModule(ctx)
  lazy val storeM = new modules.StoreModule(ctx)
  lazy val userM = new modules.UserModule(ctx)
  lazy val googleAuth = new modules.GoogleOAuthClient(
    WS.client(app),
    oAuthConfigs,
    config.getString("google.api_key").get  
  )
  
  println(config.getObject("google.client_id.json"))
  
  override def onStart() = {
    logger.info("BMotticus application started using " + Thread.currentThread.getName)
    super.onStart()
  }
  
  override def onStop () = {
    logger.info("Application Shutting down")
    super.onStop()
  }
  
}

//Context

import play.api.Play.current

trait BMotticusContext {
  def bm: plugins.BMotticus = current.plugin[BMotticus].getOrElse(throw new RuntimeException("Plugin Failed to load"))
}

object BMotticusContext extends BMotticusContext
