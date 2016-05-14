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

  def clientSignIn (redirectUrl: String) = AuthAction.async { implicit r =>
    //look for the access token in the user's session
    r.session.get(OAuth.tokenKey) match {
      case Some(token) =>
        val url = if(redirectUrl == "") routes.Dashboard.calendar().absoluteURL() else redirectUrl
        val responseF = bm.googleAuth.getResources(token)
        println(responseF)
        for{
          response <- responseF
        } yield {
          Ok(views.html.clientRedirect(response))
        }
        
      case None => {  
        val responseF = bm.googleAuth.getAuthorizationCode()
        println(responseF)
        for{
          response <- responseF
        } yield {
          (response.json \ "access_token").validate[String].fold(
            _ => InternalServerError,
            token => {
          Ok(views.html.clientSignIn(response))
            }
          )
        }
      } 
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
              val resF = bm.googleAuth.getResources(token)
              for{
                res <- resF
              } yield {
                println(res)
                Ok{views.html.clientRedirect(res)}
              }
              Redirect(routes.Dashboard.index()).addingToSession(OAuth.tokenKey -> token)
              //Ok(views.html.clientRedirect(response)).addingToSession(OAuth.tokenKey -> token)
              /**
              val responseF = bm.googleAuth.getResources(routes.OAuth.clientRedirect().url, token)
              println(responseF)
              for{
                response <- responseF
              } yield {
                Ok(views.html.clientSignIn(response))
              }
                */
            }
          )
        }
      case None =>
        Future.successful(InternalServerError)
    }
  }

}
