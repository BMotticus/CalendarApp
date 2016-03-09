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
    Ok(views.html.front.index("Your new application is ready."))
  }
  
  def signUp = Action { implicit r =>
    Ok{
    views.html.front.signUp(userForm.fill("","",""))
    }
  }
  
  def doSignUp() = Action { implicit r =>
    userForm.bindFromRequest.fold(
      f => {
        BadRequest(views.html.front.signUp(f.withGlobalError("Sign up failed.")))
      }, 
      { case (email,pass1,pass2) =>
        val userId = bm.usersM.createUser(UserData.apply(email,pass1,pass2))
        val userSession = session.SignedInUser.apply((models.User(userId,email)))
        Redirect(routes.Dashboard.userInfo(userId)).withSession(userSession.data.toList: _*)
      
      }
    )
  }
  
  def tutorials = Action { implicit r =>
    Ok(views.html.front.tutorials())
  }
  
  def contact = Action { implicit r => 
    Ok{
      views.html.front.contact(contactForm.fill(ContactData("","","","")))
    }
  }
  
  // POST
  def doContact() = Action{ implicit r =>
    contactForm.bindFromRequest().fold(
      f => BadRequest(views.html.front.contact(f)), 
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
      views.html.front.thankYou(confirm = confirm, message = message, title = title, tab = tab)
    }
  }

  def news = Action { implicit r =>
    Ok(views.html.front.news())
  }

  def signIn (path: String) = Action { implicit r =>
    Ok{
      views.html.front.signIn(path, signInForm.fill(SignInData("","")))
    }
  }
  
  def doSignIn () = Action { implicit r => 
    val path = r.body.asFormUrlEncoded.get("path").headOption.getOrElse("")
    signInForm.bindFromRequest.fold( 
      f => BadRequest(views.html.front.signIn(path, f)), 
      s => {
        val userSession = bm.usersM.signInWithCredentials(s.email, s.password).get
        //TODO optimization: reduce redirects
        Redirect(
          routes.Dashboard.clientSignIn(userSession.user.id, path)
        ).withSession(userSession.data.toList: _*)
      }
    )
  }
  
  def signOut () = Action { implicit r =>
    Redirect(routes.Application.index()) withNewSession
  }
  
}