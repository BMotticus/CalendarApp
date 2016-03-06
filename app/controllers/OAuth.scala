package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.ws.WS
import play.api.mvc.{RequestHeader, Call, Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

object OAuth {
  val tokenKey = "oauth-token"
  val authorizationEndpoint = "https://accounts.google.com/o/oauth2/auth"
  val tokenEndpoint = "https://accounts.google.com/o/oauth2/token"
  val clientId = "1079303020045-4kie53crgo06su51pi3dnbm90thc2q33.apps.googleusercontent.com"
  val clientSecret = "9-PoA1ZwynHJlE4Y3VY8fONX"

  val ws = WS.client(play.api.Play.current)

  def authorizeUrl(returnTo: Call)(implicit r: RequestHeader): String =
    makeUrl(authorizationEndpoint,
      "response_type" -> "code",
      "client_id" -> clientId,
      "redirect_uri" -> routes.OAuth.clientRedirect().absoluteURL(),
      "scope" -> "https://www.googleapis.com/auth/plus.login",
      "state" -> returnTo.url
    )

  def makeUrl(endpoint: String, qs: (String, String)*): String = {
    import java.net.URLEncoder.{encode => enc}

    val params = for ((n, v) <- qs) yield s"""${enc(n, "utf-8")}=${enc(v, "utf-8")}"""
    endpoint + params.toSeq.mkString("?", "&", "")
  }
  
}

class OAuth @Inject() (val messagesApi: MessagesApi) extends Controller with BaseController with I18nSupport {
  
  def clientRedirect = Action.async { implicit r =>
    r.getQueryString("code") match {
      case Some(code) =>
        val returnTo = r.getQueryString("state") getOrElse routes.Application.index().url
        for {
          response <- bm.googleAuth.shareCalendar(returnTo,code)
        } yield {
          (response.json \ "access_token").validate[String].fold(
            _ => InternalServerError,
            token => Redirect(returnTo).addingToSession(OAuth.tokenKey -> token)
          )
        }
      case None =>
        Future.successful(InternalServerError)
    }
  }

}
