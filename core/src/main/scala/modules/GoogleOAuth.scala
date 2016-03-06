package modules

import com.typesafe.config.ConfigObject
import play.api.libs.ws._
import com.typesafe.scalalogging.StrictLogging
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.mvc.{RequestHeader, Call}
/**
  * Scopes for Calendar API: 
  *   https://www.googleapis.com/auth/calendar
  *   https://www.googleapis.com/auth/calendar.readonly
  *
  * @param ws Web service client to make calls to google 
  * @param oAuth confidential information including client_id and client_secret 
  * @param apiKey google api key
  * @param timeout number of millis before timeout default 1000
  * @param ec implicit ExecutionContext
  */
class GoogleOAuthClient (ws: WSClient, oAuth: OAuthConfiguration, apiKey: String, timeout: Int = 1000)(implicit ec: ExecutionContext) extends StrictLogging {
  
  private def code(code: String) = "code" -> code
  
  private def calendar(url: String, token: String): Future[WSResponse] = {
    ws.url(url)
      .withRequestTimeout(timeout)
      .withQueryString(code(token), oAuth.scope, oAuth.state, oAuth.redirectUri, oAuth.responseType, oAuth.clientId, oAuth.clientSecret).get()
  }
  
  def authenticate(code: String): Future[WSResponse] = {
    calendar(oAuth.calendarUrl, code).map{ res =>
      if(res.status == 200){
        logger.debug("Successful call to Google OAuth, message: " + res)
      } else {
        logger.error(res.statusText, "Google OAuth status: " + res.status + " message: " + res.body.toString)
      }
      println(("*" * 100) + res + ("*" * 100))
      res
    }
  }

  def authorizeUrl(returnTo: String): String = {
    makeUrl(oAuth.endPoint,
      oAuth.responseType,
      oAuth.clientId,
      oAuth.redirectUri,
      "scope" -> "https://www.googleapis.com/auth/plus.login",
      "state" -> returnTo
    )
  }
  
  def makeUrl(endpoint: String, qs: (String, String)*): String = {
    import java.net.URLEncoder.{encode => enc}

    val params = for ((n, v) <- qs) yield s"""${enc(n, "utf-8")}=${enc(v, "utf-8")}"""
    endpoint + params.toSeq.mkString("?", "&", "")
  }
  /**
    * Call googles API After Authenticating with OAuth 2.0 
    *
    * @param redirectUrl URL that receives the response
    * @param token access token made by OAuth
    * @return
    */
  def shareCalendar (returnTo: String, token: String) = {
    for {
      response <- authenticate(token)
//      OAuth.ws.url(oAuth.tokenEndpoint).post(Map(
//        "code" -> Seq(token),
//        "client_id" -> Seq(oAuth.clientId),
//        "client_secret" -> Seq(oAuth.clientSecret),
//        "redirect_uri" -> Seq(oAuth.redirectUri),
//        "grant_type" -> Seq("authorization_code")
//      ))
    } yield response 
    
  }

}

class OAuthConfiguration(clientConf: ConfigObject){
  
  val endPoint = "https://accounts.google.com/o/oauth2/v2/auth"
  val tokenEndpoint = "https://accounts.google.com/o/oauth2/v2/token"
  
  val calendarUrl = "https://www.googleapis.com/auth/calendar"
  val calendarReadOnlyUrl = "https://www.googleapis.com/auth/calendar.readonly"
  
  //secure url
  val redirectUri = "redirect_uri" -> "https://localhost:9000/clientRedirect"
  
  val prompt = "prompt" -> "consent select_account"
  val scope = "scope" -> "calendar calendar.readonly"    //"email profile"
  val state = "state" -> "profile"
  val responseType = "response_type" -> "code"
  val grantType = "grant_type" -> "authorization_code"
  
  lazy val clientId = "client_id" -> getClientId
  lazy val clientSecret = "client_secret" -> getClientSecret
  
  private def getClientId = clientConf.get("client_id").render
  private def getClientSecret = clientConf.get("client_secret").render
  def getAuthUri = clientConf.get("auth_uri").render
  def getTokenUri = clientConf.get("token_uri").render
  def getAuthProviderCert = clientConf.get("auth_provider_x509_cert_url").render
  def getProjectId = clientConf.get("project_id").render
} 