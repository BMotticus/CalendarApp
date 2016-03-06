package controllers

import javax.inject.Inject

import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import scala.language.postfixOps
import play.api._
import play.api.mvc._
import play.api.libs.json
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.data._, Forms._
import org.joda.time._
import play.api.data.validation.Constraints._
import play.api.data.Form
import play.api.data.format.Formats._ 
import play.api.libs.concurrent.Execution.Implicits._

class Application  @Inject() (val messagesApi: MessagesApi) extends Controller with BaseController with I18nSupport  {
  
  val contactForm = Form(
    mapping(
      "sender" -> nonEmptyText,
      "about" -> text,
      "message" -> nonEmptyText,
      "respond" -> text
    )(ContactData.apply)(ContactData.unapply)
  )
  
  val userForm = Form(
    tuple(
      "email" -> email,
      "password1" -> nonEmptyText,
      "password2" -> text
    ).verifying("Passwords Don't Match!",f => f match {
      case (e,p1,p2) => p1 == p2
      })
  )  
  
  val signInForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(SignInData.apply)(SignInData.unapply).verifying("Username or Password was incorrect.", e => {
      bm.usersM.checkSignInCredentails(e.email,e.password).isDefined
    })
  )

  def index = Action { implicit r =>
    Ok(views.html.index("Your new application is ready."))
  }
  
  def signUp = Action { implicit r =>
    Ok{
    views.html.signUp(userForm.fill("","",""))
    }
  }
  
  def doSignUp = Action { implicit r =>
    userForm.bindFromRequest.fold(
      f => {
        BadRequest(views.html.signUp(f.withGlobalError("Sign up failed.")))
      }, 
      { case (email,pass1,pass2) =>
        println("submission successful")
        //TODO: Save to DB
        Redirect(routes.Application.userInfo(bm.usersM.createUser(UserData.apply(email,pass1,pass2))))
      }
    )
  }
  
  def schedule = Action { implicit r =>
    Ok(views.html.schedule())
  }
  
  def userInfo(userId: Long) = Action { implicit r =>
    val user = bm.usersM.findUserById(userId)
    Ok{
      views.html.userInfo(user)
    }
  }
  
  def contact = Action { implicit r => 
    Ok{
      views.html.contact(contactForm.fill(ContactData("","","","")))
    }
  }
  
  // POST
  def doContact = Action{ implicit r =>
    contactForm.bindFromRequest().fold(
      f => BadRequest(views.html.contact(f)), 
      s => {
        val confirm = bm.usersM.createMessage(s)
        Redirect(routes.Application.thankYou(confirm = confirm,
        message = "Thank you for contacting us, we will respond as soon as possible. Have a great day",
        title = "Message Sent", tab = "contact"))
      }
    )
  }
  
  def thankYou(confirm: Long, message: String, title: String, tab: String) = Action {implicit r =>
    Ok{
      views.html.thankYou(confirm = confirm, message = message, title = title, tab = tab)
    }
  }

  def messageBoard = Action { implicit r =>
    Ok(views.html.messageBoard())
  }

  def signIn (path: String) = Action { implicit r =>
    Ok{
      views.html.signIn(path, signInForm.fill(SignInData("","")))
    }
  }
  
  def doSignIn () = Action { implicit r => 
    val path = r.body.asFormUrlEncoded.get("path").headOption
    signInForm.bindFromRequest.fold( 
      f => BadRequest(views.html.signIn(path.getOrElse(""), f)), 
      s => {
        val userSession = bm.usersM.signInWithCredentials(s.email, s.password).get
        //TODO: Implement path redirect
        
        Redirect(
          routes.Application.index().url
        ).withSession(userSession.data.toList: _*)
      }
    )
  }
  
  def signOut () = Action { implicit r =>
    Redirect(routes.Application.index()) withNewSession
  }
  
  //get calendar infomation
  def calendar(clientId: Long) = Action { implicit r =>
    Ok(views.html.calendar())
  }

  def clientSignIn (clientId: Long) = Action { implicit r =>
    //val clientId = authenticate()
    r.session.get(OAuth.tokenKey) match {
      case Some(token) =>
        val url = routes.Application.calendar(clientId).absoluteURL()
        bm.googleAuth.shareCalendar(url, token)
        Ok
      case None => 
        Redirect(bm.googleAuth.authorizeUrl(routes.Application.calendar(clientId).url))
    }
  }
}