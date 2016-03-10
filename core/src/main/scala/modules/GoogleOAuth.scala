package modules

import com.typesafe.config.ConfigObject
import play.api.libs.ws._
import com.typesafe.scalalogging.StrictLogging
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.mvc.{RequestHeader, Call}
import play.api.libs.oauth.ServiceInfo

/**
  * Scopes for Calendar API: 
  *   https://www.googleapis.com/auth/calendar
  *   https://www.googleapis.com/auth/calendar.readonly
  *
  * @param ws Web service client to make calls to google 
  * @param oAuth confidential information including client_id and client_secret 
  * @param apiKey google api key
  * @param timeout number of millis before timeout default 3000
  * @param ec implicit ExecutionContext
  */
class GoogleOAuthClient (
  ws: WSClient, 
  oAuth: OAuthConfiguration, 
  apiKey: String, 
  timeout: Int = 3000
)(implicit ec: ExecutionContext) extends StrictLogging {

  /**
    * STEP 1: make url to get authorization_code 
    * @param returnTo URL that called the web service
    * @return authorization code used to get access token
    */
  def getAuthorizationCode(returnTo: String): String = {
    makeUrl(oAuth.getAuthUri,
      oAuth.responseType,
      oAuth.clientId,
      oAuth.redirectUri,
      "scope" -> oAuth.loginEndPoint,
      "state" -> returnTo
    )
  }

  /**
    * function used in STEP 1: constructs url to get authorization_code 
    * 
    * @param endpoint authorization uri
    * @param qs query string parameters
    * @return string representation of URL encoded in utf-8
    */
  def makeUrl(endpoint: String, qs: (String, String)*): String = {
    import java.net.URLEncoder.{encode => enc}

    val params = for ((n, v) <- qs) yield s"""${enc(n, "utf-8")}=${enc(v, "utf-8")}"""
    endpoint + params.toSeq.mkString("?", "&", "")
  }
  //TODO store date in db
  /**
    * STEP 2: Request and store access_token
    *
    * @param authCode authorization_code used to get access token
    * @return Future[WSResponse]
    */
  def requestAccessToken (authCode: String): Future[WSResponse] = {
    for {
      response <- getAccessToken(authCode)
    } yield response
  }

  /**
    * function used in STEP 2: use WSClient and send authorization code to token uri to get access token
    * @param code authorization code used to get access token
    * @return Future[WSResponse]
    */
  def getAccessToken(code: String): Future[WSResponse] = {
    ws.url(oAuth.getTokenUri)
      .withRequestTimeout(timeout)
      .withQueryString(oAuth.code(code), oAuth.clientId, oAuth.clientSecret, oAuth.redirectUri, oAuth.grantType).get()
  }

  /**
    * Calling Google's API After receiving Access token
    * @param token access token
    * @param returnTo returnTo URL
    * @return
    */
  def getResources(token: String, returnTo: String): Future[WSResponse] = {
    calendarAPI(oAuth.apiEndPoint, token, returnTo, oAuth.calendarUrl).map{ res =>
      if(res.status == 200){
        logger.debug("Successful call to Google OAuth, message: " + res)
        println(("*" * 100) + "Success, message: " + res + ("*" * 100))
      } else {
        logger.error(res.statusText, "Google OAuth status: " + res.status + " message: " + res.body.toString)
        println(("*" * 100) + "Google OAuth status: " + res.status + " message: " + res.body.toString + ("*" * 100))
      }
      res
    }
  }

  /**
    * 
    * @param url
    * @param token
    * @param returnTo
    * @param scope
    * @return
    */
  def calendarAPI(url: String, token: String, returnTo: String, scope: String): Future[WSResponse] = {
    ws.url(url)
      .withRequestTimeout(timeout)
      .withQueryString(oAuth.code(token), oAuth.clientId, oAuth.clientSecret, oAuth.scope(scope), oAuth.redirectUri, oAuth.responseType).get()
  }

}

class OAuthConfiguration(clientConf: ConfigObject){
  
  val loginEndPoint = "https://www.googleapis.com/auth/plus.login"
  //val authCodeEndPoint = "https://accounts.google.com/o/oauth2/v2/auth"
  //val tokenEndpoint = "https://www.googleapis.com/oauth2/v4/token"
  val apiEndPoint = "https://www.googleapis.com/drive/v2/files"
  
  val calendarUrl = "https://www.googleapis.com/auth/calendar"
  val calendarReadOnlyUrl = "https://www.googleapis.com/auth/calendar.readonly"
  
  //secure callback url
  val redirectUri = "redirect_uri" -> "https://localhost:9000/clientRedirect"
  
  val prompt = "prompt" -> "consent select_account"
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

  def scope(scope: String) = "scope" -> scope
  def code(code: String) = "code" -> code
  def state(state:String) = "state" -> state
} 