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
  
  def clientRedirect() = Action.async { implicit r =>
    r.getQueryString("code") match {
      case Some(authCode) =>
        val returnTo = r.getQueryString("state") getOrElse routes.Application.index().url
        for {
          response <- bm.googleAuth.requestAccessToken(authCode)
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
