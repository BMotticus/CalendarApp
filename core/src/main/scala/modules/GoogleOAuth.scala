package modules

import com.typesafe.config.ConfigObject
import play.api.libs.ws._
import com.typesafe.scalalogging.StrictLogging
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.mvc.{RequestHeader, Call}
import play.api.libs.oauth.ServiceInfo

trait GoogleOAuth {
  /**
    * STEP 1: Request authorization_code from google 
    * @example https://accounts.google.com/o/oauth2/auth?
    * @1 redirect_uri = {uri}
    * @2 response_type = "code" 
    * @3 client_id = {clientId}
    * @4 scope = "email profile" //s"${oAuth.calendarUrl} ${oAuth.calendarReadOnlyUrl}"
    * @5 approval_prompt = "force" 
    * @6 access_type = "offline"
    * @return authorization code used to get access token
    */
  def requestAuthorizationCode(): Future[WSResponse]
  /**
    * STEP 2: Request and store access_token
    * use WSClient and send authorization code to token uri to get access token
    * @param authCode authorization_code used to get access token
    * @return Future[WSResponse]
    */
  def requestAccessToken (authCode: String): Future[WSResponse]
  /**
    * STEP 3: Calling Google's API After receiving Access token
    * @param token access token
    * @return
    */
  def requestResources(token: String): Future[WSResponse]
  
  /**
    * function constructs url encoded in utf-8
    * @param endpoint url
    * @param qs query string 
    * @return string representation of URL encoded in utf-8
    */
  def makeUrl(endpoint: String, qs: (String, String)*): String
  
}

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
)(implicit ec: ExecutionContext) extends GoogleOAuth with StrictLogging {
  def clientId = oAuth.getClientId
  
  //TODO: Test and finish this request
  def requestAuthorizationCode(): Future[WSResponse] = {
    val responseF = ws.url("https://accounts.google.com/o/oauth2/auth")
      .withRequestTimeout(timeout)
      .withQueryString(oAuth.scope("email profile"), oAuth.redirectUri, oAuth.responseType, oAuth.clientId, oAuth.clientSecret, oAuth.promptConsent).get()
    for {
      response <- responseF
    } yield response
    //TODO Decompose, move to STEP 2
  }


  def makeUrl(endpoint: String, qs: (String, String)*): String = {
    import java.net.URLEncoder.{encode => enc}

    val params = for ((n, v) <- qs) yield s"""${enc(n, "utf-8")}=${enc(v, "utf-8")}"""
    endpoint + params.toSeq.mkString("?", "&", "")
  }
  
  
  def requestAccessToken (authCode: String): Future[WSResponse] = {
    val responseF = ws.url(oAuth.getTokenUri)
      .withRequestTimeout(timeout)
      .withQueryString("key" -> apiKey, oAuth.code(authCode), oAuth.clientId, oAuth.clientSecret, oAuth.redirectUri, oAuth.grantType).get()
    for {
      response <- responseF
    } yield response
    //TODO Decompose, store in db, move to STEP 3
  }
  
  def requestResources(token: String): Future[WSResponse] = {
    val responseF = ws.url("https://localhost:9000/clientRedirect")
    .withRequestTimeout(timeout)
    .withQueryString(oAuth.code(token), oAuth.clientId, oAuth.clientSecret, oAuth.scope(oAuth.calendarUrl), oAuth.redirectUri, oAuth.responseType).get()
    for {
      response <- responseF
    } yield {
      if(response.status == 200){
        println("Successful call to Google OAuth, message: " + response)
      } else {
        println("Failed. Google OAuth status: " + response.statusText + " message: " + response.body.toString)
      }
      response
      //TODO Decompose / DRY this
    }
  }

}


//TODO: Figure out what to do with this mess.
class OAuthConfiguration(clientConf: ConfigObject){

  val loginEndPoint = "https://www.googleapis.com/auth/plus.login"
  val apiEndPoint = "https://www.googleapis.com/drive/v2/files"
  val authorizationCodeUrl = "https://accounts.google.com/o/oauth2/v2/auth"
  val tokenEndpoint = "https://www.googleapis.com/oauth2/v4/token"


  val calendarUrl = "https://www.googleapis.com/auth/calendar"
  val calendarReadOnlyUrl = "https://www.googleapis.com/auth/calendar.readonly"

  //TODO: check HTTPS callback url
  val redirectUri = "redirect_uri" -> "https://localhost:9000/clientRedirect"

  val approvalForce = "approval_prompt" -> "force"
  val accessTypeOffline = "access_type" -> "offline"
  val promptConsent = "prompt" -> "consent select_account"
  val responseType = "response_type" -> "code"
  val grantType = "grant_type" -> "authorization_code"

  lazy val clientId = "client_id" -> getClientId
  lazy val clientSecret = "client_secret" -> getClientSecret

  def getClientId = clientConf.get("client_id").render
  def getClientSecret = clientConf.get("client_secret").render
  def getAuthUri = clientConf.get("auth_uri").render
  def getTokenUri = clientConf.get("token_uri").render
  def getAuthProviderCert = clientConf.get("auth_provider_x509_cert_url").render
  def getProjectId = clientConf.get("project_id").render

  def scope(scope: String) = "scope" -> scope
  def code(code: String) = "code" -> code
  def state(state:String) = "state" -> state
}