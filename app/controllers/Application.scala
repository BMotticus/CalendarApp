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
import play.api.data._
import Forms._
import org.joda.time._
import play.api.data.validation.Constraints._
import play.api.data.Form
import play.api.data.format.Formats._
import play.api.libs.concurrent.Execution.Implicits._
import java.time.{ZoneId, ZoneOffset}

class Application  @Inject() (val messagesApi: MessagesApi) extends Controller with BaseController with I18nSupport  {
  
  val contactForm = Form(
    mapping(
      "sender" -> nonEmptyText,
      "about" -> text,
      "message" -> nonEmptyText,
      "respond" -> text
    )(ContactData.apply)(ContactData.unapply)
  )
  
  val signUpForm = Form(
    tuple(
      "companyName" -> nonEmptyText,
      "email" -> email,
      "password1" -> nonEmptyText,
      "password2" -> text,
      "timezone" -> text
    ).verifying("Passwords Don't Match!",f => f match {
      case (cn,e,p1,p2,tz) => p1 == p2
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
    views.html.front.signUp(signUpForm.fill("","","","",""))
    }
  }
  
  def doSignUp() = Action { implicit r =>
    signUpForm.bindFromRequest.fold(
      f => {
        BadRequest(views.html.front.signUp(f.withGlobalError("Sign up failed.")))
      }, 
      { case (cn,e,p1,p2,tz) =>
        val timezone = ZoneId.of(ZoneOffset.ofHours(tz.toInt).getId)
        val userSession = bm.accountsM.createAccount(SignUpData.apply(cn,e,p1,timezone))
        Redirect(routes.Dashboard.userInfo()).withSession(userSession.data.toList: _*)
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
      views.html.front.thankYou(confirm=confirm, message=message, title=title, tab=tab)
    }
  }

  def news = Action { implicit r =>
    Ok(views.html.front.news())
  }
  
  def signIn (path: String) = Action { implicit r =>
    Ok{
      views.html.front.signIn( path, signInForm.fill(SignInData("","")), bm.googleAuth.clientId )
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
          routes.OAuth.clientSignIn(path)
        ).withSession(userSession.data.toList: _*)
      }
    )
  }
  
  def signOut () = Action { implicit r =>
    Redirect(routes.Application.index()) withNewSession
  }
  
}