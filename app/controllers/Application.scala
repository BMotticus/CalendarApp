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
      "username" -> text,
      "email" -> text,
      "password1" -> text,
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
  
  import mysql._
  import com.gravitydev.scoop._, query._
  import play.api.Play.current
  import play.api.db.DB
  
  def createUser(user: UserData): Long = {
    println("Inside create user")
    DB.withConnection{ implicit conn =>
      using(tables.users){u => 
      insertInto(u)
        .values(
          u.user_name  := user.userName,
          u.email     := user.email,
          u.password  := user.password,
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
}