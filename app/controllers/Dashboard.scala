package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

/**
  * Created by brandonmott1 on 3/8/16.
  */
class Dashboard  @Inject() (val messagesApi: MessagesApi) extends Controller with BaseController with I18nSupport  {

  def index() = AuthAction {implicit r => 
    
    Ok{views.html.dashboard.index()}
  }

  def userInfo() = AuthAction { implicit r =>
    val user = bm.userM.byId(r.user.id)
    val store = bm.storeM.byId(r.store.id)
    
    Ok{
      views.html.dashboard.userInfo(user, store)
    }
  }
  
  def updateInfo() = AuthAction {implicit r =>
    val user = bm.usersM.findUserById(r.user.id)
    Ok{
      views.html.dashboard.updateInfo()
    }
  }
  
  def calendars() = AuthAction {implicit r => 
    
    Ok{views.html.dashboard.calendars()}
  }
  
  def calendar() = AuthAction { implicit r =>
    
    Ok(views.html.dashboard.calendar())
  }

  def schedules() = AuthAction {implicit r =>

    Ok{views.html.dashboard.schedules()}
  }

  def messageBoard() = AuthAction {implicit r =>

    Ok{views.html.dashboard.messageBoard()}
  }

  def contacts() = AuthAction {implicit r =>

    Ok{views.html.dashboard.contacts()}
  }
}
