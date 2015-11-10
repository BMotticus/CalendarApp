package controllers

import models._
import play.api.data.validation.Constraints._
import com.typesafe.scalalogging.StrictLogging
import scala.language.postfixOps
import play.api._
import play.api.mvc._
import org.joda.time.{LocalDate, DateTime, DateTimeZone}
import play.api.data.Form
import play.api.data.format.Formats._ 
import play.api.data.Forms._
import mysql._
import com.gravitydev.scoop._, query._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.db.DB
import java.sql.Connection

object Application extends Controller with StrictLogging {

  val userForm = Form(
    tuple(
      "username" -> text,
      "email" -> text,
      "password" -> text,
      "password2" -> text
    )//(UserData.apply)(UserData.unapply).verifying("Passwords must match", fields => fields.password == fields.password2 )
  )
  
  case class UserData(
    userName: String,
    email: String,
    password: String,
    password2: String
  )  
  
  

  def index = Action { implicit r =>
    Ok(views.html.index("Your new application is ready."))
  }
  
  def signUp = Action { implicit r =>
    Ok{
    views.html.signUp(userForm.fill("","","",""))
    }
  }
  
  def doSignUp = Action (parse.urlFormEncoded) {implicit r =>
    userForm.bindFromRequest.fold(
      f => {
        logger.debug("submission failed: " + f)
        BadRequest(views.html.signUp(f.withGlobalError("Sign up failed.")))
      }, 
      { case (username,email,pass1,pass2) =>
        logger.debug("submission successful")
        //TODO: Save to DB
        UserData.apply(username,email,pass1,pass2)
        Redirect(routes.Application.index)
      }
    )
  }
  
  def createUser(user: UserData) = {
    logger.debug("Inside create user")
    val userId = DB.withConnection{ implicit conn =>
      using(tables.users){u => 
      insertInto(u)
        .values(
          u.user_name  := user.userName,
          u.email     := user.email,
          u.created_date := DateTime.now
        )().get
      }
    }
  }
  
}