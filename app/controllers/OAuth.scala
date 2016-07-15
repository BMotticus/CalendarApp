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
  //TODO: Question - Do I need this and controllers.Application.signIn()?
  def clientSignIn (redirectUrl: String) = AuthAction.async { implicit r =>
    //look for the access token in the user's session
    r.session.get(OAuth.tokenKey) match {
      case Some(token) =>
        val url = if(redirectUrl == "") routes.Dashboard.calendar().absoluteURL() else redirectUrl
        val responseF = bm.googleAuth.requestResources(token)
        for{
          response <- responseF
        } yield {
          println("Resources" + response)
          Ok(views.html.clientRedirect(response))
        }

      case None => {
        val responseF = bm.googleAuth.requestAuthorizationCode()
        for{
          response <- responseF
        } yield {
          println("AuthCode" + response)
          Ok(views.html.clientSignIn(response))
        }
      }
    }
  }
  
  def clientResources(redirectUrl: String, scope: String) = AuthAction.async { implicit r =>
    //look for the access token in the user's session
    r.session.get(OAuth.tokenKey) match {
      case Some(token) =>
        val url = if(redirectUrl == "") routes.Dashboard.calendar().absoluteURL() else redirectUrl
        val responseF = bm.googleAuth.requestResources(token)
        for{
          response <- responseF
        } yield {
          println("Resources" + response)
          Ok(views.html.clientRedirect(response))
        }

      case None => {
        val responseF = bm.googleAuth.requestAuthorizationCode()
        for{
          response <- responseF
        } yield {
          println("AuthCode" + response)
          Ok(views.html.clientSignIn(response))
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
            _ => Ok(views.html.clientRedirect(response)), //InternalServerError,Ok(views.html.clientRedirect(response))
            token => {
              println(response)
              for{
                res <- bm.googleAuth.requestResources(token)
              } yield {
                println(res)
                Ok{views.html.clientRedirect(res)}
              }
              //Redirect(routes.Dashboard.index()),Ok(views.html.clientRedirect(response))
              Ok(views.html.clientRedirect(response)).addingToSession(OAuth.tokenKey -> token)
            }
          )
        }
      case None =>
        Future.successful(InternalServerError)
    }
  }
  
}
