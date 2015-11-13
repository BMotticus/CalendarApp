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
import play.api.Play.current
import java.sql.Connection

object Application extends Controller with StrictLogging {

  //val bm = current.plugin[bmotticus.BMPlugin].get
  
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
  val contactForm = Form(
    mapping(
      "sender" -> nonEmptyText,
      "about" -> text,
      "message" -> nonEmptyText,
      "respond" -> text
    )(ContactData.apply)(ContactData.unapply)
  )
  

  def index = Action { implicit r =>
    Ok(views.html.index("Your new application is ready."))
  }
  
  def signUp = Action { implicit r =>
    Ok{
    views.html.signUp(userForm.fill("","","",""))
    }
  }
  
  def doSignUp = Action {implicit r =>
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
  
  def contact = Action{ implicit r => 
    Ok{
      views.html.contact(contactForm.fill(ContactData("","","","")))
    }
  }
  
  def doContact = Action{ implicit r =>
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
  
  
  import mysql._
  import com.gravitydev.scoop._, query._
  import play.api.Play.current
  import play.api.db.DB
  
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
}