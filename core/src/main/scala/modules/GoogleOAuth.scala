package modules

import com.typesafe.config.ConfigObject
import play.api.libs.ws._
import com.typesafe.scalalogging.StrictLogging
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
  * Scopes for Calendar API: 
  *   https://www.googleapis.com/auth/calendar
  *   https://www.googleapis.com/auth/calendar.readonly
  * @param ws Web service client to make calls to google 
  * @param clientConf confidential information including client_id and client_secret 
  * @param apiKey google api key
  * @param timeout number of millis before timeout default 1000
  * @param ec implicit ExecutionContext
  */
class GoogleOAuthClient (ws: WSClient, clientConf: ConfigObject, apiKey: String, timeout: Int = 1000)(implicit ec: ExecutionContext) extends StrictLogging {
  
  val oAuth = new OAuthConfiguration(clientConf)
  
  private def call(url: String): Future[WSResponse] = {
    ws.url(url)
      .withRequestTimeout(timeout)
      .withQueryString(oAuth.scope, oAuth.state, oAuth.redirectUri, oAuth.responseType, oAuth.clientId, oAuth.clientSecret).get()
  }
  
  def authenticate() = {
    call(oAuth.url).map{ res =>
      if(res.status == 200){
        logger.debug("Successful call to Google OAuth, message: " + res)
      } else {
        logger.error(res.statusText, "Google OAuth status: " + res.status + " message: " + res.body.toString)
      }
      println(("*" * 100) + res + ("*" * 100))
      res
    }
  }

  /**
    * Call googles API After Authenticating with OAuth 2.0 
    * @param redirectUrl URL that receives the response
    * @param token access token made by OAuth
    * @return
    */
  def shareCalendar (redirectUrl: String, token: String) = ???
}


class OAuthConfiguration(clientConf: ConfigObject){
  val url = "https://accounts.google.com/o/oauth2/v2/auth"
  val calendarUrl = "https://www.googleapis.com/auth/calendar"
  val calendarReadOnlyUrl = "https://www.googleapis.com/auth/calendar.readonly"
  //secure url
  val redirectUri = "redirect_uri" -> "https://localhost:9000/clientRedirect"
  val prompt = "prompt" -> "consent select_account"
  val scope = "scope" -> "calendar calendar.readonly"    //"email profile"
  val state = "state" -> "profile"
  val responseType = "response_type" -> "code"
  
  lazy val clientId = "client_id" -> getClientId
  lazy val clientSecret = "client_secret" -> getClientSecret
  
  def getClientId = clientConf.get("client_id").render
  def getClientSecret = clientConf.get("client_secret").render
  def getAuthUri = clientConf.get("auth_uri").render
  def getTokenUri = clientConf.get("token_uri").render
  def getAuthProviderCert = clientConf.get("auth_provider_x509_cert_url").render
  def getProjectId = clientConf.get("project_id").render
}