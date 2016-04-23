package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

/**
  * Created by brandonmott1 on 3/8/16.
  */
class Dashboard  @Inject() (val messagesApi: MessagesApi) extends Controller with BaseController with I18nSupport  {
  
  def index(userId: Long) = AuthAction {implicit r => 
    
    Ok{views.html.dashboard.index()}
  }

  def userInfo(userId: Long) = AuthAction { implicit r =>
    val user = bm.userM.byId(userId)
    val store = bm.storeM.byUserId(userId)
    
    Ok{
      views.html.dashboard.userInfo(user, store)
    }
  }
  
  def updateInfo(userId: Long, storeId: Long) = AuthAction {implicit r =>
    val user = bm.usersM.findUserById(userId)
    Ok{
      views.html.dashboard.updateInfo()
    }
  }
  
  def calendars(userId: Long) = AuthAction {implicit r => 
    
    Ok{views.html.dashboard.calendars()}
  }
  
  def calendar(userId: Long) = AuthAction { implicit r =>
    
    Ok(views.html.dashboard.calendar())
  }

  def clientSignIn (userId: Long, redirectUrl: String) = AuthAction { implicit r =>
    
    //look for the access token in the user's session
    r.session.get(OAuth.tokenKey) match {
      case Some(token) =>
        val url = if(redirectUrl == "") routes.Dashboard.calendar(userId).absoluteURL() else redirectUrl
        bm.googleAuth.getResources(url, token)
        Ok
      case None =>
        Redirect(bm.googleAuth.getAuthorizationCode(routes.Dashboard.calendar(userId).url))
    }
  }
  
  def schedules(userId: Long) = AuthAction {implicit r =>

    Ok{views.html.dashboard.schedules()}
  }

  def messageBoard(userId: Long) = AuthAction {implicit r =>

    Ok{views.html.dashboard.messageBoard()}
  }

  def contacts(userId: Long) = AuthAction {implicit r =>

    Ok{views.html.dashboard.contacts()}
  }
}
