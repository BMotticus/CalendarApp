package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.ws.WS
import play.api.mvc.{RequestHeader, Call, Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

object OAuth {
  val tokenKey = "oauth-token"
}

class OAuth @Inject() (val messagesApi: MessagesApi) extends Controller with BaseController with I18nSupport {

  def clientSignIn (redirectUrl: String) = AuthAction { implicit r =>
    //look for the access token in the user's session
    r.session.get(OAuth.tokenKey) match {
      case Some(token) =>
        val url = if(redirectUrl == "") routes.Dashboard.calendar().absoluteURL() else redirectUrl
        bm.googleAuth.getResources(url, token)
        Ok
      case None =>
        Redirect(bm.googleAuth.getAuthorizationCode(routes.OAuth.clientRedirect().url))
    }
  }
  
  def clientRedirect() = Action.async { implicit r =>
    r.getQueryString("code") match {
      case Some(authCode) =>
        val returnTo = r.getQueryString("state") getOrElse routes.Dashboard.index().url
        for {
          response <- bm.googleAuth.requestAccessToken(authCode)
        } yield {
          (response.json \ "access_token").validate[String].fold(
            _ => InternalServerError,
            token => {
              println(response)
              Redirect(routes.Dashboard.index()).addingToSession(OAuth.tokenKey -> token)
            }
          )
        }
      case None =>
        Future.successful(InternalServerError)
    }
  }

}
