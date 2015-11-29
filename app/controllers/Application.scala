package controllers

import com.typesafe.scalalogging.StrictLogging
import scala.language.postfixOps
import play.api._
import play.api.mvc._
import org.joda.time._
import play.api.data.validation.Constraints._
import play.api.data.Form
import play.api.data.format.Formats._ 
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller with BaseController with StrictLogging {

  case class UserData(
    username: String,
    email: String,
    password1: String,
    password2: String
  )  
  
  case class ContactData(
    sender: String,
    about: String,
    message: String,
    respond: String
  )
  
  case class SignInData(username: String, password:String)
  
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
      "username" -> nonEmptyText,
      "email" -> email,
      "password1" -> nonEmptyText,
      "password2" -> text
    ).verifying("Passwords Don't Match!",f => f match {
      case (u,e,p1,p2) => p1 == p2
      })
  )  
  
  val signInForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(SignInData.apply)(SignInData.unapply).verifying("Username or Password was incorrect.", e => {
      checkSignInCredentails(e.username,e.password).isDefined
    })
  )

  def index = Action { implicit r =>
    Ok(views.html.index("Your new application is ready."))
  }
  
  def signUp = Action { implicit r =>
    Ok{
    views.html.signUp(userForm.fill("","","",""))
    }
  }
  
  def doSignUp = Action { implicit r =>
    userForm.bindFromRequest.fold(
      f => {
        println("submission failed: " + f)
        BadRequest(views.html.signUp(f.withGlobalError("Sign up failed.")))
      }, 
      { case (username,email,pass1,pass2) =>
        println("submission successful")
        //TODO: Save to DB
        Redirect(routes.Application.userInfo(createUser(UserData.apply(username,email,pass1,pass2))))
      }
    )
  }
  
  def userInfo(userId: Long) = Action { implicit r =>
    val user = findUserById(userId)
    Ok{
      views.html.userInfo(user)
    }
  }
  
  def contact = Action { implicit r => 
    Ok{
      views.html.contact(contactForm.fill(ContactData("","","","")))
    }
  }
  
  def doContact = Action { implicit r =>
    contactForm.bindFromRequest().fold(
      f => BadRequest(views.html.contact(f)), 
      s => {
        val confirm = createMessage(s)
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

  def tutorials = Action { implicit r =>
    Ok(views.html.tutorials())
  }

  def documents = Action { implicit r =>
    Ok(views.html.documents())
  }

  def blog = Action { implicit r =>
    Ok(views.html.blog())
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
        val userSession = signInWithCredentials(s.username, s.password).get
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
  
  /**
   * Database 
   */
  import mysql._
  import com.gravitydev.scoop._, query._
  import play.api.Play.current
  import play.api.db.DB
  import java.sql.Connection
  
  def createUser(user: UserData): Long = {
    println("Creating user")
    DB.withConnection{ implicit conn =>
      using(tables.users){u => 
      insertInto(u)
        .values(
          u.user_name  := user.username,
          u.email     := user.email,
          u.password  := user.password1,
          u.created_date := DateTime.now
        )().get
      }
    }
  }
  
  def findUserById(userId: Long): models.User = {
    DB.withTransaction{implicit conn =>
      using (tables.users) {u => 
        from(u)
          .where(u.id === userId)
          .find(models.Parsers.user(u))
          .headOption getOrElse sys.error("No User found for id: " + userId)
      }
    }
  }
  
  def createMessage(data: ContactData): Long = {
    DB.withConnection{ implicit conn =>
      using(tables.messages) {m =>
        insertInto(m)
          .values(
            m.sender_info := data.sender,
            m.about := Option(data.about).filter(_.nonEmpty),
            m.message := data.message,
            m.respond_info := Option(data.respond).filter(_.nonEmpty),
            m.sent_date := DateTime.now 
          )().get
      }
    }
  }
  
  def checkSignInCredentails(username: String,password: String): Option[models.User] = {
    DB.withTransaction{ implicit conn => 
      using (tables.users) {u => 
        from(u)
          .where(u.user_name === username && u.password === password)
          .find(models.Parsers.user(u))
          .headOption
      }
    }
  }
  
  def signInWithCredentials(username: String, password:String): Option[session.SignedInUser] = {
    DB.withTransaction{ implicit conn => 
      using (tables.users) {u => 
        from(u)
          .where(u.user_name === username && u.password === password)
          .find(models.Parsers.user(u) >> session.SignedInUser.apply)
          .headOption
      }
    }
  }
   
}